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

import org.hibernate.Hibernate;
import org.springframework.dao.EmptyResultDataAccessException;

import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.ImportantNamedEntity;
import com.mothsoft.alexis.domain.ImportantTerm;
import com.mothsoft.alexis.domain.TopicDocument;
import com.mothsoft.alexis.domain.Tweet;
import com.mothsoft.alexis.service.DocumentService;

public class ViewDocumentDetailsBackingBean {

    // dependency
    private DocumentService documentService;

    // state
    private String id;
    private Document document;

    private List<ImportantNamedEntity> importantNamedEntities;
    private Integer importantNamedEntitiesMaxCount = Integer.valueOf(0);

    private List<ImportantTerm> importantTerms;

    private List<TopicDocument> topicDocuments;

    public ViewDocumentDetailsBackingBean() {
        // default
    }

    public final void setDocumentService(final DocumentService documentService) {
        this.documentService = documentService;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Document getDocument() {
        if (this.document == null) {
            this.document = this.documentService.getDocument(this.id);

            if (document instanceof Tweet) {
                final Tweet tweet = (Tweet) document;
                Hibernate.initialize(tweet.getHashtags());
                Hibernate.initialize(tweet.getLinks());
                Hibernate.initialize(tweet.getMentions());
            }

            this.importantTerms = this.document.getImportantTerms();
            this.importantNamedEntities = this.document.getImportantNamedEntities();

            try {
                this.topicDocuments = documentService.getTopicDocuments(id);
            } catch (EmptyResultDataAccessException e) {
                this.topicDocuments = new ArrayList<TopicDocument>(0);
            }
        }

        return this.document;
    }

    public List<ImportantNamedEntity> getImportantNamedEntities() {
        return this.importantNamedEntities;
    }

    public Integer getImportantNamedEntitiesMaxCount() {
        return this.importantNamedEntitiesMaxCount;
    }

    public List<ImportantTerm> getImportantTerms() {
        return this.importantTerms;
    }

    public List<TopicDocument> getTopicDocuments() {
        return this.topicDocuments;
    }

}
