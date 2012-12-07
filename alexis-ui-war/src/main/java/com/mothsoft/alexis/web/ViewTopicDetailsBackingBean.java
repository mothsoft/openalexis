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
package com.mothsoft.alexis.web;

import java.util.ArrayList;
import java.util.List;

import com.mothsoft.alexis.domain.DataRange;
import com.mothsoft.alexis.domain.DataSetType;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentScore;
import com.mothsoft.alexis.domain.SortOrder;
import com.mothsoft.alexis.domain.Topic;
import com.mothsoft.alexis.domain.TopicActivityDataSet;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.DataSetService;
import com.mothsoft.alexis.service.DocumentService;
import com.mothsoft.alexis.service.TopicService;

public class ViewTopicDetailsBackingBean {

    // dependency
    private TopicService topicService;
    private DataSetService dataSetService;
    private DocumentService documentService;

    // state
    private Long id;
    private Topic topic;
    private TopicActivityDataSet dataSet;
    private List<Document> documents;

    public ViewTopicDetailsBackingBean() {
        // default
    }

    public final void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }

    public final void setTopicService(final TopicService topicService) {
        this.topicService = topicService;
    }

    public final void setDataSetService(final DataSetService dataSetService) {
        this.dataSetService = dataSetService;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
        this.topic = null;
        this.documents = null;
    }

    public Topic getTopic() {
        if (this.topic == null) {
            this.topic = this.topicService.get(this.id);
        }

        if (!CurrentUserUtil.getCurrentUserId().equals(this.topic.getUserId())) {
            throw new SecurityException("Access Denied");
        }

        return this.topic;
    }

    public TopicActivityDataSet getDataSet() {
        if (this.dataSet == null) {
            this.dataSet = this.dataSetService.findTopicActivityDataSet(this.id);

            if (this.dataSet == null) {
                this.dataSet = new TopicActivityDataSet(this.topicService.get(this.id),
                        this.dataSetService.findDataSetType(DataSetType.TOPIC_ACTIVITY));
                this.dataSetService.addDataSet(this.dataSet);
            }
        }

        return this.dataSet;
    }

    public List<Document> getDocuments() {
        if (this.documents == null) {
            final Topic topic = getTopic();
            final String query = topic.getSearchExpression();
            final Long userId = CurrentUserUtil.getCurrentUserId();

            final DataRange<DocumentScore> range = this.documentService.searchByOwnerAndExpression(userId, query,
                    SortOrder.DATE_DESC, 0, 10);
            final List<Document> tempList = new ArrayList<Document>(range.getRange().size());
            for (final DocumentScore ith : range.getRange()) {
                tempList.add(ith.getDocument());
            }
            this.documents = tempList;
        }

        return this.documents;
    }
}
