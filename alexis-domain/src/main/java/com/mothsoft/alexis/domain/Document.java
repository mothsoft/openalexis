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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

@Entity(name = "Document")
@Table(name = "document")
@Inheritance(strategy = InheritanceType.JOINED)
@Indexed
public class Document {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "url", length = 4096)
    private String url;

    @Lob
    @Column(name = "title", columnDefinition = "text")
    @Field(store = Store.NO)
    private String title;

    @Lob
    @Column(name = "description", columnDefinition = "text")
    @Field(store = Store.NO)
    private String description;

    @Column(name = "state", columnDefinition = "tinyint")
    @Field(name = "state")
    @FieldBridge(impl = DocumentStateFieldBridge.class)
    private int intState;

    private transient DocumentState state;

    @Column(name = "creation_date")
    @Field(name = "creationDate")
    @FieldBridge(impl = DateAsLongFieldBridge.class)
    private Date creationDate;

    @Column(name = "retrieval_date")
    private Date retrievalDate;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Lob
    @Column(name = "etag", columnDefinition = "text")
    private String etag;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn(name = "document_id")
    @IndexedEmbedded(prefix = "content.")
    private DocumentContent documentContent;

    @Column(name = "content_length")
    private Integer contentLength;

    @Column(name = "term_count")
    private Integer termCount;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "document")
    @OrderBy("associationWeight desc")
    private List<DocumentAssociation> documentAssociations;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "document")
    @OrderBy("tfIdf desc")
    private List<DocumentTerm> documentTerms;

    // FIXME - there has to be a proper way to filter/left join these for
    // permissioning. couldn't make it work. if i had it probably would have
    // broken caching potential anyway...
    @OneToMany(cascade = {}, mappedBy = "document", fetch = FetchType.LAZY)
    @OrderBy("score DESC")
    @Field(name = "topicUser")
    @FieldBridge(impl = TopicDocumentFieldBridge.class)
    private List<TopicDocument> topicDocuments;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    @Field(name = "user")
    @FieldBridge(impl = DocumentUserFieldBridge.class)
    private List<DocumentUser> documentUsers;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "document")
    @OrderBy("count DESC")
    private List<DocumentNamedEntity> documentNamedEntities;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", columnDefinition = "char(1)")
    @FieldBridge(impl = DocumentStateFieldBridge.class)
    private DocumentType type = DocumentType.W;

    @Version
    @Column(name = "version", columnDefinition = "smallint unsigned")
    protected Integer version;

    @Column(name = "indexed", columnDefinition = "bit")
    protected boolean indexed = true;

    public Document(final DocumentType type, final URL url, final String title, final String description) {
        this.type = type;
        this.url = url.toExternalForm();

        this.state = DocumentState.DISCOVERED;

        this.title = title;
        this.description = description;
        this.contentLength = -1;
        this.termCount = -1;
        this.documentUsers = new ArrayList<DocumentUser>();
    }

    protected Document() {
        this.documentAssociations = Collections.emptyList();
        this.documentTerms = Collections.emptyList();
        this.documentNamedEntities = Collections.emptyList();
    }

    @PrePersist
    @PreUpdate
    protected void prePersist() {
        this.intState = this.state.getValue();
    }

    @PostLoad
    protected void postLoad() {
        this.state = DocumentState.getByValue(this.intState);
    }

    public Long getId() {
        return this.id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getRetrievalDate() {
        return this.retrievalDate;
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

    public DocumentContent getDocumentContent() {
        return this.documentContent;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public Integer getTermCount() {
        return this.termCount;
    }

    public String getUrl() {
        return url;
    }

    public List<DocumentAssociation> getDocumentAssociations() {
        return this.documentAssociations;
    }

    public List<DocumentTerm> getDocumentTerms() {
        return this.documentTerms;
    }

    public DocumentState getState() {
        return state;
    }

    public int getIntState() {
        return intState;
    }

    public String getTitle() {
        return title;
    }

    public DocumentType getType() {
        return this.type;
    }

    public String getDescription() {
        return description;
    }

    public String getText() {
        return this.documentContent == null ? "" : this.documentContent.getText();
    }

    public List<TopicDocument> getTopicDocuments() {
        return topicDocuments;
    }

    public List<DocumentUser> getDocumentUsers() {
        return this.documentUsers;
    }

    public void lock() {
        this.intState = DocumentState.LOCKED.getValue();
        this.state = DocumentState.LOCKED;
    }

    public void onErrorState(final DocumentState state) {
        if (state.getValue() < 50 || state.equals(DocumentState.LOCKED)) {
            throw new IllegalStateException("Invalid state " + state.toString()
                    + " suggested, probably not an error state");
        }
        this.intState = state.getValue();
        this.state = state;
    }

    public void setDocumentContent(final DocumentContent documentContent) {
        this.documentContent = documentContent;
    }

    public void setParsedContent(final ParsedContent parsedContent) {

        if (this.termCount > 0) {
            throw new IllegalStateException("This document has already been initialized!");
        }

        this.documentAssociations = new ArrayList<DocumentAssociation>(parsedContent.getDocumentAssociations());
        for (final DocumentAssociation documentAssociation : this.documentAssociations) {
            documentAssociation.setDocument(this);
        }

        this.documentTerms = new ArrayList<DocumentTerm>(parsedContent.getTerms());
        for (final DocumentTerm documentTerm : this.documentTerms) {
            documentTerm.setDocument(this);
        }

        this.documentNamedEntities = new ArrayList<DocumentNamedEntity>(parsedContent.getNamedEntities());
        for (final DocumentNamedEntity entity : this.documentNamedEntities) {
            entity.setDocument(this);
        }

        this.termCount = parsedContent.getDocumentTermCount();

        this.state = DocumentState.PARSED;
        this.intState = this.state.getValue();
    }

    public void setState(final DocumentState nextState) {
        this.state = nextState;
        this.intState = this.state.getValue();
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public Integer getVersion() {
        return this.version;
    }

    public List<DocumentNamedEntity> getNamedEntities() {
        return documentNamedEntities;
    }
}
