/*  Copyright 2012 Tim Garrett, Mothsoft LLC
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.dao.DocumentDao;
import com.mothsoft.alexis.dao.TopicDao;
import com.mothsoft.alexis.domain.DataRange;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentScore;
import com.mothsoft.alexis.domain.SortOrder;
import com.mothsoft.alexis.domain.Topic;
import com.mothsoft.alexis.domain.TopicDocument;
import com.mothsoft.alexis.domain.TopicRef;

public class TopicDocumentMatcherImpl implements TopicDocumentMatcher {

    private static final String QUERY_PATTERN = "+id:%s +(%s)";

    private TopicDao topicDao;
    private DocumentDao documentDao;

    public TopicDocumentMatcherImpl(final TopicDao topicDao, final DocumentDao documentDao) {
        this.topicDao = topicDao;
        this.documentDao = documentDao;
    }

    @Transactional
    @Override
    public List<TopicRef> match(final Document document, final Long userId) {
        final List<TopicRef> topicRefs = new ArrayList<TopicRef>();
        final List<Topic> topics = this.topicDao.listTopicsByOwner(userId);

        for (final Topic topic : topics) {
            final TopicRef topicRef = this.matchAndScore(topic, document);

            if (topicRef != null && topicRef.getScore() > 0.0f) {
                topicRefs.add(topicRef);

                final TopicDocument topicDocument = new TopicDocument();
                topicDocument.setTopic(topic);
                topicDocument.setDocumentId(document.getId());
                topicDocument.setScore(topicRef.getScore());
                topicDocument.setCreationDate(new Date());
                this.topicDao.add(topicDocument);
            }
        }

        return topicRefs;
    }

    private TopicRef matchAndScore(Topic topic, Document document) {
        final String query = String.format(QUERY_PATTERN, document.getId(), topic.getSearchExpression());
        final DataRange<DocumentScore> result = this.documentDao.searchByOwnerAndExpression(topic.getUserId(), query,
                SortOrder.RELEVANCE, 1, 1);

        if (result.getRange().size() == 1) {
            DocumentScore first = result.getRange().get(0);
            return new TopicRef(topic.getId(), first.getScore(), new Date());
        }
        return null;
    }

}