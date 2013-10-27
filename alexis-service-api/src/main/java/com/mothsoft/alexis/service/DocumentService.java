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
package com.mothsoft.alexis.service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.mothsoft.alexis.domain.DataRange;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentScore;
import com.mothsoft.alexis.domain.Graph;
import com.mothsoft.alexis.domain.ImportantNamedEntity;
import com.mothsoft.alexis.domain.ImportantTerm;
import com.mothsoft.alexis.domain.SortOrder;
import com.mothsoft.alexis.domain.TopicDocument;

public interface DocumentService {

    public Document getDocument(String id);

    public List<ImportantTerm> getImportantTerms(Long userId, Timestamp startDate, Timestamp endDate, int count,
            boolean filterStopWords);

    public Graph getRelatedTerms(final String queryString, final Long userId, final int howMany);

    public Double getSimilarity(final String aId, final String bId);

    public List<TopicDocument> getTopicDocuments(final String documentId);

    public DataRange<Document> listDocumentsByOwner(Long userId, int firstRecord, int numberOfRecords);

    public DataRange<Document> listDocumentsInTopicsByOwner(Long userId, int firstRecord, int numberOfRecords);

    public List<Document> listTopDocuments(Long userId, final Date startDate, final Date endDate, int count);

    public DataRange<DocumentScore> searchByOwnerAndExpression(Long userId, String query, SortOrder sortOrder,
            int first, int count);

    public List<ImportantNamedEntity> getImportantNamedEntities(Long userId, Date startDate, Date endDate, int howMany);

}
