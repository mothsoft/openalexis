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

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.domain.DateConstants;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.ImportantNamedEntity;
import com.mothsoft.alexis.domain.TopicActivityDataSet;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.DataSetService;
import com.mothsoft.alexis.service.DocumentService;

public class DashboardBackingBean {

    private DocumentService documentService;
    private DataSetService dataSetService;

    private List<Document> topRecentContent;
    private List<TopicActivityDataSet> mostActiveDataSets;
    private String mostActiveTopicsQueryParameters;
    private List<ImportantNamedEntity> topNames;

    public DashboardBackingBean() {
    }

    public void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setDataSetService(final DataSetService dataSetService) {
        this.dataSetService = dataSetService;
    }

    public List<Document> getTopRecentContent() {
        if (this.topRecentContent == null) {
            final Long userId = CurrentUserUtil.getCurrentUserId();
            final Date end = new Date();
            final Date start = new Date(end.getTime() - DateConstants.ONE_DAY_IN_MILLISECONDS);
            this.topRecentContent = this.documentService.listTopDocuments(userId, start, end, 10);
        }

        return this.topRecentContent;
    }

    public List<TopicActivityDataSet> getMostActiveDataSets() {
        if (this.mostActiveDataSets == null) {
            final Long userId = CurrentUserUtil.getCurrentUserId();
            final Long now = System.currentTimeMillis();
            final Timestamp timestampNow = new Timestamp(now);
            final Timestamp twelveHoursAgo = new Timestamp(now - DateConstants.TWELVE_HOURS_IN_MILLISECONDS);

            // FIXME - just doing topic activity for now -- could potentially
            // normalize units and do all data sets?
            this.mostActiveDataSets = this.dataSetService.findMostActiveTopicDataSets(userId, twelveHoursAgo,
                    timestampNow, 5);
        }
        return this.mostActiveDataSets;
    }

    public String getMostActiveDataSetsQueryParameters() {
        if (this.mostActiveTopicsQueryParameters == null) {
            final StringBuilder builder = new StringBuilder();

            for (int i = 0; i < this.getMostActiveDataSets().size(); i++) {
                final DataSet ith = this.getMostActiveDataSets().get(i);
                builder.append("&ds=").append(ith.getId());
            }

            this.mostActiveTopicsQueryParameters = builder.toString();
        }
        return this.mostActiveTopicsQueryParameters;
    }

    public Integer getTopNamedEntityMaxCount() {
        int result = 0;
        for (final ImportantNamedEntity entity : getTopNames()) {
            if (entity.getCount() > result) {
                result = entity.getCount();
            }
        }
        return Integer.valueOf(result);
    }

    public List<ImportantNamedEntity> getTopNames() {
        if (this.topNames == null) {
            final Long userId = CurrentUserUtil.getCurrentUserId();
            final Long now = System.currentTimeMillis();
            final Timestamp timestampNow = new Timestamp(now);
            final Timestamp twelveHoursAgo = new Timestamp(now - DateConstants.TWELVE_HOURS_IN_MILLISECONDS);
            this.topNames = this.documentService.getImportantNamedEntities(userId, twelveHoursAgo, timestampNow, 42);

            Collections.sort(this.topNames, new Comparator<ImportantNamedEntity>() {
                @Override
                public int compare(ImportantNamedEntity e1, ImportantNamedEntity e2) {
                    return e1.getName().compareTo(e2.getName());
                }
            });
        }
        return this.topNames;
    }
}
