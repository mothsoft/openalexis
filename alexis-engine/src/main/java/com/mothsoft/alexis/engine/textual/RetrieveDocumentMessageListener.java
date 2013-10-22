/*   Copyright 2013 Tim Garrett, Mothsoft LLC
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
package com.mothsoft.alexis.engine.textual;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import com.mothsoft.alexis.dao.DocumentDao;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.util.HttpClientResponse;
import com.mothsoft.alexis.util.NetworkingUtil;

public class RetrieveDocumentMessageListener implements SessionAwareMessageListener<TextMessage> {

    private static final Logger logger = Logger.getLogger(ParseResponseMessageListener.class);

    private static final String DOCUMENT_ID = "DOCUMENT_ID";
    private static final String TEXT_HTML = "text/html";

    private DocumentDao documentDao;

    public RetrieveDocumentMessageListener(DocumentDao documentDao) {
        logger.info("Started RetrieveDocumentMessageListener!");
        this.documentDao = documentDao;
    }

    @Override
    @Transactional
    public void onMessage(final TextMessage message, final Session session) throws JMSException {
        final String documentId = message.getStringProperty(DOCUMENT_ID);

        Document document = this.documentDao.get(documentId);
        String fetched;
        try {
            fetched = this.fetch(document);
            this.documentDao.addRawContent(documentId, document.getRev(), fetched, TEXT_HTML);
        } catch (Exception e) {
            logger.warn("Exception retrieving: " + documentId + " rev " + document.getRev() + ": " + e, e);
            throw new JMSException(e.getLocalizedMessage());
        }

    }

    private String fetch(final Document document) throws MalformedURLException, IOException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final HttpClientResponse response = NetworkingUtil.get(new URL(document.getUrl()), document.getEtag(),
                document.getLastModifiedDate());
        final String result = IOUtils.toString(response.getInputStream());

        stopWatch.stop();
        logger.info("Fetching document ID: " + document.getId() + " took " + stopWatch.getTotalTimeSeconds()
                + " seconds");

        return result;
    }
}
