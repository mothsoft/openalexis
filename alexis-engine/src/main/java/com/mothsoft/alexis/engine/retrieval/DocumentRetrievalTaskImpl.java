/*   Copyright 2012 Tim Garrett, Mothsoft LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mothsoft.alexis.engine.retrieval;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.mothsoft.alexis.dao.DocumentDao;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentContent;
import com.mothsoft.alexis.domain.DocumentState;
import com.mothsoft.alexis.engine.textual.WebContentParser;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.util.HttpClientResponse;
import com.mothsoft.alexis.util.NetworkingUtil;

public class DocumentRetrievalTaskImpl implements RetrievalTask {

    private static final Logger logger = Logger.getLogger(DocumentRetrievalTaskImpl.class);

    private static final String DOCUMENT_ID = "DOCUMENT_ID";

    private ConnectionFactory connectionFactory;
    private Destination requestQueue;
    private Destination responseQueue;

    private DocumentDao documentDao;
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;

    private WebContentParser webContentParser;

    private IntelligentDelay delay;

    public DocumentRetrievalTaskImpl() {
        super();
        delay = new IntelligentDelay("Document Retrieval", 2, 30);
    }

    public void setConnectionFactory(final ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setRequestQueue(final Destination requestQueue) {
        this.requestQueue = requestQueue;
    }

    public void setResponseQueue(final Destination responseQueue) {
        this.responseQueue = responseQueue;
    }

    public void setDocumentDao(final DocumentDao documentDao) {
        this.documentDao = documentDao;
    }

    public void setWebContentParser(final WebContentParser webContentParser) {
        this.webContentParser = webContentParser;
    }

    public void setTransactionManager(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.transactionTemplate = new TransactionTemplate(this.transactionManager);
    }

    public void retrieve() {
        try {
            CurrentUserUtil.setSystemUserAuthentication();

            // retrieve up to 100 per scheduler call, returning as soon as none
            // are found
            boolean fetching = true;
            int remaining = 100;

            while (fetching && remaining > 0) {
                fetching = doRetrieve();
                remaining--;
            }

            if (!fetching) {
                logger.info("Document Retrieval found nothing to do, will return");
            }

        } finally {
            CurrentUserUtil.clearAuthentication();
        }
    }

    private boolean doRetrieve() {

        boolean foundSomething = false;
        Document document = null;

        try {
            logger.info("Looking for documents pending retrieval");

            document = findDocumentToRetrieve();

            if (document == null) {
                foundSomething = false;
            } else {
                foundSomething = true;
                handleDocument(document);
            }
        } catch (final Exception e) {
            if (document != null) {
                foundSomething = true;
                logger.warn("Document: " + document.getId() + " failed retrieval, will be set to error state");
                onErrorState(document.getId(), DocumentState.ERROR_RETRIEVAL_FAILED);
            } else {
                // not finding documents, but erroring. might indicate subsystem
                // problem (database, disk, etc.)
                logger.warn("Document not found or handled improperly, may retry again -- this may be FATAL!");
            }

            // throttle on error conditions
            logger.warn("Throttling document retrieval on error condition: " + e, e);
            delay.sleep();
        }

        return foundSomething;
    }

    private void handleDocument(final Document document) throws IOException {

        logger.info("Retrieving document: " + document.getId() + ", URL: " + document.getUrl());

        // allow for content already ingested but not parsed
        String entryContent = document.getText();
        String etag = null;
        Date lastModifiedDate = null;
        final Date retrievalDate = new Date();

        if (StringUtils.isEmpty(entryContent)) {
            final URL url = new URL(document.getUrl());

            HttpClientResponse response = null;
            InputStream is = null;

            try {
                response = NetworkingUtil.get(url, null, null);

                is = response.getInputStream();
                entryContent = this.webContentParser.parse(is);

                etag = response.getEtag();
                lastModifiedDate = response.getLastModifiedDate();

                logger.debug("Document " + document.getId() + " has: " + entryContent.length() + " characters");
            } catch (IOException e) {
                response.abort();
                logger.error("IOException while retrieving URL: " + url + " " + e, e);
                onErrorState(document.getId(), DocumentState.ERROR_PARSE_FAILED);
            } finally {
                IOUtils.closeQuietly(response);
            }
        }

        updateStateAndQueueForParsing(document.getId(), entryContent, etag, lastModifiedDate, retrievalDate);
    }

    private Document findDocumentToRetrieve() {
        return this.transactionTemplate.execute(new TransactionCallback<Document>() {

            public Document doInTransaction(TransactionStatus txStatus) {
                return DocumentRetrievalTaskImpl.this.documentDao.findAndLockOneDocument(DocumentState.DISCOVERED);
            }

        });
    }

    private void onErrorState(final Long documentId, final DocumentState errorState) {
        logger.warn("Setting error state: " + errorState.toString() + " on document " + documentId);
        this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus txStatus) {
                final Document attachedDocument = DocumentRetrievalTaskImpl.this.documentDao.get(documentId);
                attachedDocument.onErrorState(errorState);
                DocumentRetrievalTaskImpl.this.documentDao.update(attachedDocument);
            }
        });
    }

    private void updateStateAndQueueForParsing(final Long documentId, final String content, final String etag,
            final Date lastModifiedDate, final Date retrievalDate) {

        this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus txStatus) {
                final Document attachedDocument = DocumentRetrievalTaskImpl.this.documentDao.get(documentId);
                attachedDocument.setEtag(etag);
                attachedDocument.setLastModifiedDate(lastModifiedDate);
                attachedDocument.setRetrievalDate(retrievalDate);

                if (attachedDocument.getDocumentContent() == null) {
                    final DocumentContent documentContent = new DocumentContent(attachedDocument, content);
                    attachedDocument.setDocumentContent(documentContent);
                    DocumentRetrievalTaskImpl.this.documentDao.add(documentContent);
                }

                DocumentRetrievalTaskImpl.this.documentDao.update(attachedDocument);

                // do in transaction to make sure failure to queue doesn't leave
                // dead doc
                requestParse(documentId, content);
            }
        });
    }

    private String requestParse(final Long documentId, final String content) {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        // set up JMS connection, session, consumer, producer
        try {
            connection = this.connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(this.requestQueue);

            logger.info("Sending parse request, document ID: " + documentId);
            final TextMessage textMessage = session.createTextMessage(content);
            textMessage.setJMSReplyTo(this.responseQueue);
            textMessage.setLongProperty(DOCUMENT_ID, documentId);

            producer.send(textMessage);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (producer != null) {
                    producer.close();
                }
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }

        return content;
    }

}
