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

import com.mothsoft.alexis.domain.DataRange;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentScore;
import com.mothsoft.alexis.domain.Graph;
import com.mothsoft.alexis.domain.ImportantNamedEntity;
import com.mothsoft.alexis.domain.ImportantTerm;
import com.mothsoft.alexis.domain.ParsedContent;
import com.mothsoft.alexis.domain.SortOrder;
import com.mothsoft.alexis.domain.TopicRef;
import com.mothsoft.alexis.domain.Tweet;

public interface DocumentDao {

    /* CRUD */
    public void add(Document document);

    /** Add a Tweet and queue it for NLP processing */
    public void add(Tweet tweet);

    public void addRawContent(String documentId, String rev, String content, String mimeType);

    public String getRawContent(String documentId);

    /** Add document content and queue for NLP processing */
    public void addContent(String documentId, String rev, String content, String mimeType);

    public String getContent(String documentId);

    public void addParsedContent(String documentId, ParsedContent parsedContent);

    public ParsedContent getParsedContent(String documentId);

    public Document findByUrl(String url);

    public Tweet findTweetByTweetId(Long tweetId);

    public Document get(String documentId);

    public void remove(Document document);

    public void update(Document document);

    /* Search */
    public DataRange<Document> listDocumentsByOwner(Long userId, int start, int count);

    public DataRange<Document> listDocumentsInTopicsByOwner(Long userId, int firstRecord, int numberOfRecords);

    public List<Document> listTopDocuments(Long userId, Date startDate, Date endDate, int count);

    public DataRange<DocumentScore> search(Long userId, String query, Date startDate, Date endDate, SortOrder sortOrder,
            int first, int count);

    /* NLP */

    public List<ImportantNamedEntity> getImportantNamedEntities(Long userId, Date startDate, Date endDate, int howMany);

    public List<ImportantTerm> getImportantTerms(Long userId, Date startDate, Date endDate, int count,
            boolean filterStopWords);

    public Graph getRelatedTerms(String query, Long userId, int howMany);

    public List<TopicRef> getTopics(Document document, Long userId);

    public int getDocumentCount();

    public int termCount(String term);
}
