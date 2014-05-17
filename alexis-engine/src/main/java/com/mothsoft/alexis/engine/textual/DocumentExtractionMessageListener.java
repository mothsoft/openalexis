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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import com.mothsoft.alexis.dao.DocumentDao;
import com.mothsoft.alexis.domain.Document;

public class DocumentExtractionMessageListener implements SessionAwareMessageListener<TextMessage> {

    private static final Logger logger = Logger.getLogger(DocumentExtractionMessageListener.class);

    private static final String DOCUMENT_ID = "DOCUMENT_ID";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String UTF8 = "UTF-8";

    private DocumentDao documentDao;
    private WebContentParser webContentParser;

    public DocumentExtractionMessageListener(DocumentDao documentDao) {
        logger.info("Started RetrieveDocumentMessageListener!");
        this.documentDao = documentDao;
        this.webContentParser = new WebContentParserImpl();
    }

    @Override
    @Transactional
    public void onMessage(final TextMessage message, final Session session) throws JMSException {
        final String documentId = message.getStringProperty(DOCUMENT_ID);

        Document document = this.documentDao.get(documentId);
        String extracted;
        try {
            extracted = this.extract(document);
            this.documentDao.addContent(document.getId(), document.getRev(), extracted, TEXT_PLAIN);
        } catch (Exception e) {
            logger.warn("Exception extracting: " + documentId + " rev " + document.getRev() + ": " + e, e);
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }

    }

    private String extract(final Document document) throws MalformedURLException, IOException {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final String content = this.documentDao.getRawContent(document.getId());
        final InputStream is = new ByteArrayInputStream(content.getBytes(UTF8));
        final String result = this.webContentParser.parse(is);

        stopWatch.stop();
        logger.info("Extracting content for document ID: " + document.getId() + " took "
                + stopWatch.getTotalTimeSeconds() + " seconds");

        return result;
    }

}
