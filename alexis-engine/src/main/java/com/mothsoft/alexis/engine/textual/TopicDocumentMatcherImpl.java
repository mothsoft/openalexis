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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.hibernate.ScrollableResults;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.mothsoft.alexis.dao.DocumentDao;
import com.mothsoft.alexis.dao.TopicDao;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentScore;
import com.mothsoft.alexis.domain.DocumentState;
import com.mothsoft.alexis.domain.SortOrder;
import com.mothsoft.alexis.domain.Topic;
import com.mothsoft.alexis.domain.TopicDocument;
import com.mothsoft.alexis.engine.Task;
import com.mothsoft.alexis.security.CurrentUserUtil;

/**
 * @author tgarrett
 */
public class TopicDocumentMatcherImpl implements Task {

    private static final Logger logger = Logger.getLogger(TopicDocumentMatcherImpl.class);

    private TopicDao topicDao;
    private DocumentDao documentDao;
    private TransactionTemplate transactionTemplate;

    public TopicDocumentMatcherImpl() throws IOException {
    }

    public void setDocumentDao(final DocumentDao documentDao) {
        this.documentDao = documentDao;
    }

    public void setTopicDao(final TopicDao topicDao) {
        this.topicDao = topicDao;
    }

    public void setTransactionManager(final PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public void execute() {

        logger.info("Starting Topic<=>Document Matching");

        final StopWatch stopWatch = new StopWatch();

        // a unique state for documents pending matching so we can transition
        // items that had no topics (prevent a big future spike when a topic is
        // added that now matches really old documents)
        stopWatch.start();
        bulkUpdateDocumentState(DocumentState.PARSED, DocumentState.PENDING_TOPIC_MATCHING);
        stopWatch.stop();
        logger.info("Marking PARSED documents as PENDING_TOPIC_MATCHING took: " + stopWatch.toString());
        stopWatch.reset();

        stopWatch.start();
        match();
        stopWatch.stop();
        logger.info("Matching documents and topics took: " + stopWatch.toString());
        stopWatch.reset();

        // update any documents that had no assignments
        stopWatch.start();
        bulkUpdateDocumentState(DocumentState.PENDING_TOPIC_MATCHING, DocumentState.MATCHED_TO_TOPICS);
        stopWatch.stop();
        logger.info("Marking PENDING_TOPIC_MATCHING documents as MATCHED_TO_TOPICS took: " + stopWatch.toString());
    }

    private void bulkUpdateDocumentState(final DocumentState queryState, final DocumentState nextState) {
        this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus txStatus) {
                TopicDocumentMatcherImpl.this.documentDao.bulkUpdateDocumentState(queryState, nextState);
            }
        });
    }

    private void match() {
        this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus txStatus) {
                try {
                    CurrentUserUtil.setSystemUserAuthentication();

                    final Map<Long, List<TopicScore>> documentTopicMap = new HashMap<Long, List<TopicScore>>();

                    final List<Topic> topics = TopicDocumentMatcherImpl.this.topicDao.list();
                    for (final Topic topic : topics) {
                        mapMatches(topic, documentTopicMap);
                    }

                    saveMatches(documentTopicMap);

                    final long rowsAffected = documentTopicMap.size();
                    logger.info("Topic<=>Document matching associated " + rowsAffected + " items");
                } finally {
                    CurrentUserUtil.clearAuthentication();
                }
            }

        });
    }

    private void mapMatches(final Topic topic, final Map<Long, List<TopicScore>> documentTopicMap) {
        final String query = topic.getSearchExpression();

        final ScrollableResults scrollableResults = this.documentDao.scrollableSearch(topic.getUserId(),
                DocumentState.PENDING_TOPIC_MATCHING, query, SortOrder.DATE_ASC, null, null);

        try {
            while (scrollableResults.next()) {
                final Object[] array = scrollableResults.get();

                // allow for state document index
                if (array[0] == null) {
                    continue;
                }

                final DocumentScore documentScore = new DocumentScore((Document) array[0], (Float) array[1]);
                mapMatches(topic, documentScore, documentTopicMap);
            }
        } finally {
            scrollableResults.close();
        }
    }

    private void mapMatches(final Topic topic, final DocumentScore documentScore,
            final Map<Long, List<TopicScore>> documentTopicMap) {
        final Long documentId = documentScore.getDocument().getId();

        if (!documentTopicMap.containsKey(documentId)) {
            documentTopicMap.put(documentId, new ArrayList<TopicScore>(16));
        }

        final TopicScore topicScore = new TopicScore(topic, documentScore.getScore());
        documentTopicMap.get(documentId).add(topicScore);
    }

    private void saveMatches(final Map<Long, List<TopicScore>> documentTopicMap) {
        final Date now = new Date();

        // try to reduce contention by always starting with the oldest doc ID
        // and processing in the same order as other bulk operations
        final List<Long> sortedDocumentIds = new ArrayList<Long>(documentTopicMap.keySet());
        Collections.sort(sortedDocumentIds);

        for (final Long documentId : sortedDocumentIds) {
            final Document document = this.documentDao.get(documentId);

            final List<TopicScore> topicScores = documentTopicMap.get(documentId);
            for (final TopicScore topicScore : topicScores) {
                final Topic topic = topicScore.getTopic();
                final Float score = topicScore.getScore();

                final TopicDocument topicDocument = new TopicDocument(topic, document, score);
                this.topicDao.add(topicDocument);

                topic.setLastDocumentMatchDate(now);
                this.topicDao.update(topic);
            }

            document.setState(DocumentState.MATCHED_TO_TOPICS);
            this.documentDao.update(document);
        }
    }

    private class TopicScore {
        private Topic topic;
        private Float score;

        TopicScore(final Topic topic, final float score) {
            this.topic = topic;
            this.score = score;
        }

        Topic getTopic() {
            return this.topic;
        }

        Float getScore() {
            return this.score;
        }
    }

}
