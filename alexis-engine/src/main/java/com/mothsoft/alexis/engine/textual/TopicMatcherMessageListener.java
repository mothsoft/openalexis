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
package com.mothsoft.alexis.engine.textual;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.dao.DocumentDao;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentState;
import com.mothsoft.alexis.domain.DocumentUser;
import com.mothsoft.alexis.domain.TopicRef;

public class TopicMatcherMessageListener implements SessionAwareMessageListener<TextMessage> {

    private static final Logger logger = Logger.getLogger(TopicMatcherMessageListener.class);

    private static final String DOCUMENT_ID = "DOCUMENT_ID";

    private DocumentDao documentDao;
    private TopicDocumentMatcher topicDocumentMatcher;

    public TopicMatcherMessageListener(final DocumentDao documentDao, final TopicDocumentMatcher topicDocumentMatcher) {
        this.documentDao = documentDao;
        this.topicDocumentMatcher = topicDocumentMatcher;
    }

    /**
     * Match topics and save back to the document
     **/
    @Override
    @Transactional
    public void onMessage(final TextMessage message, final Session session) throws JMSException {

        final String documentId = message.getStringProperty(DOCUMENT_ID);
        logger.info("Received topic matcher request for document ID: " + documentId);

        final Document document = this.documentDao.get(documentId);
        for (final DocumentUser documentUser : document.getDocumentUsers()) {
            final List<TopicRef> topics = this.topicDocumentMatcher.match(document, documentUser.getUserId());
            documentUser.setTopics(topics);
        }

        document.setState(DocumentState.MATCHED_TO_TOPICS);
        this.documentDao.update(document);
    }
}
