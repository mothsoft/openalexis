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
package com.mothsoft.alexis.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.ScrollableResults;

import com.mothsoft.alexis.domain.DataRange;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentContent;
import com.mothsoft.alexis.domain.DocumentScore;
import com.mothsoft.alexis.domain.DocumentState;
import com.mothsoft.alexis.domain.Graph;
import com.mothsoft.alexis.domain.ImportantNamedEntity;
import com.mothsoft.alexis.domain.ImportantTerm;
import com.mothsoft.alexis.domain.SortOrder;
import com.mothsoft.alexis.domain.TopicDocument;

public interface DocumentDao {

    public void add(Document document);

    public void add(DocumentContent content);

    public void bulkUpdateDocumentState(DocumentState queryState, DocumentState nextState);

    public Document findByUrl(String url);

    public Document findAndLockOneDocument(final DocumentState state);

    public Document get(Long id);

    public List<ImportantTerm> getImportantTerms(Long userId, Date startDate, Date endDate, int count,
            boolean filterStopWords);

    public List<ImportantTerm> getImportantTerms(Long documentId, int howMany, boolean filterStopWords);

    public Graph getRelatedTerms(String query, Long userId, int howMany);

    public List<TopicDocument> getTopicDocuments(Long documentId);

    public DataRange<Document> listDocumentsByOwner(Long userId, int first, int count);

    public DataRange<Document> listDocumentsInTopicsByOwner(Long userId, int firstRecord, int numberOfRecords);

    public List<Document> listTopDocuments(Long userId, Date startDate, Date endDate, int count);

    public ScrollableResults scrollableSearch(Long userId, DocumentState state, String queryString,
            SortOrder sortOrder, Date startDate, Date endDate);

    public DataRange<DocumentScore> searchByOwnerAndExpression(Long userId, String query, SortOrder sortOrder,
            Date startDate, Date endDate, int first, int count);

    public DataRange<DocumentScore> searchByOwnerAndStateAndExpression(Long userId, DocumentState state, String query,
            Date startDate, Date endDate, int first, int count);

    public int searchResultCount(Long userId, DocumentState state, String queryString, Date startDate, Date endDate);

    public void update(Document document);

    public List<ImportantNamedEntity> getImportantNamedEntities(Long userId, Date startDate, Date endDate, int howMany);

    public List<ImportantNamedEntity> getImportantNamedEntitiesForDocument(Long documentId, int howMany);

}
