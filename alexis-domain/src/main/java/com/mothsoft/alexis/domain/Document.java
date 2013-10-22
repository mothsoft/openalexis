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
package com.mothsoft.alexis.domain;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {

    @JsonProperty("_id")
    String id;

    @JsonProperty("_rev")
    private String rev;

    private String url;

    private String title;

    private String description;

    private String content;

    private DocumentState state;

    private Date creationDate;

    private Date retrievalDate;

    private Date lastModifiedDate;

    private String etag;

    private Integer termCount;

    @JsonProperty("associations")
    private List<DocumentAssociation> documentAssociations;

    @JsonProperty("terms")
    private List<DocumentTerm> documentTerms;

    private List<TopicDocument> topicDocuments;

    @JsonProperty("users")
    private Set<DocumentUser> documentUsers;

    @JsonProperty("namedEntities")
    private List<DocumentNamedEntity> documentNamedEntities;

    private DocumentType type = DocumentType.W;

    @JsonProperty("_attachments")
    private Map<String, Object> attachments;

    public Document(final DocumentType type, final URL url, final String title, final String description) {
        this.type = type;
        this.url = url.toExternalForm();

        this.state = DocumentState.DISCOVERED;

        this.title = title;
        this.description = description;
        this.termCount = -1;
        this.documentUsers = new HashSet<DocumentUser>();
    }

    protected Document() {
        this.documentAssociations = Collections.emptyList();
        this.documentTerms = Collections.emptyList();
        this.documentNamedEntities = Collections.emptyList();
    }

    public void onErrorState(final DocumentState state) {
        if (state.getValue() < 50 || state.equals(DocumentState.LOCKED)) {
            throw new IllegalStateException("Invalid state " + state.toString()
                    + " suggested, probably not an error state");
        }
        this.state = state;
    }

    public void setParsedContent(final ParsedContent parsedContent) {

        if (this.termCount > 0) {
            throw new IllegalStateException("This document has already been initialized!");
        }

        this.documentAssociations = new ArrayList<DocumentAssociation>(parsedContent.getDocumentAssociations());
        for (final DocumentAssociation documentAssociation : this.documentAssociations) {
            documentAssociation.setDocumentId(this.id);
        }

        this.documentTerms = new ArrayList<DocumentTerm>(parsedContent.getTerms());
        for (final DocumentTerm documentTerm : this.documentTerms) {
            documentTerm.setDocumentId(this.id);
        }

        this.documentNamedEntities = new ArrayList<DocumentNamedEntity>(parsedContent.getNamedEntities());
        for (final DocumentNamedEntity entity : this.documentNamedEntities) {
            entity.setDocumentId(this.id);
        }

        this.termCount = parsedContent.getDocumentTermCount();

        this.state = DocumentState.PARSED;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public DocumentState getState() {
        return state;
    }

    public void setState(DocumentState state) {
        this.state = state;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getRetrievalDate() {
        return retrievalDate;
    }

    public void setRetrievalDate(Date retrievalDate) {
        this.retrievalDate = retrievalDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Integer getTermCount() {
        return termCount;
    }

    public void setTermCount(Integer termCount) {
        this.termCount = termCount;
    }

    public List<DocumentAssociation> getDocumentAssociations() {
        return documentAssociations;
    }

    public void setDocumentAssociations(List<DocumentAssociation> documentAssociations) {
        this.documentAssociations = documentAssociations;
    }

    public List<DocumentTerm> getDocumentTerms() {
        return documentTerms;
    }

    public void setDocumentTerms(List<DocumentTerm> documentTerms) {
        this.documentTerms = documentTerms;
    }

    public List<TopicDocument> getTopicDocuments() {
        return topicDocuments;
    }

    public void setTopicDocuments(List<TopicDocument> topicDocuments) {
        this.topicDocuments = topicDocuments;
    }

    public Set<DocumentUser> getDocumentUsers() {
        return documentUsers;
    }

    public void setDocumentUsers(Set<DocumentUser> documentUsers) {
        this.documentUsers = documentUsers;
    }

    public List<DocumentNamedEntity> getDocumentNamedEntities() {
        return documentNamedEntities;
    }

    public void setDocumentNamedEntities(List<DocumentNamedEntity> documentNamedEntities) {
        this.documentNamedEntities = documentNamedEntities;
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

}
