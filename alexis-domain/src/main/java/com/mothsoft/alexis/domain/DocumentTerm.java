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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity(name = "DocumentTerm")
@Table(name = "document_term")
public class DocumentTerm implements Comparable<DocumentTerm> {

    @Id
    DocumentTermId id;

    @ManyToOne
    @JoinColumn(name = "document_id", insertable = false, updatable = false)
    private Document document;

    @ManyToOne
    @JoinColumn(name = "term_id", insertable = false, updatable = false, columnDefinition = "integer unsigned")
    private Term term;

    @Column(name = "term_count")
    private Integer count;

    @Column(name = "tf_idf")
    private Float tfIdf;

    public DocumentTerm(final Term term, final Integer count) {
        this.term = term;
        this.count = count;
    }

    protected DocumentTerm() {
        super();
    }
    
    @PrePersist
    public void prePersist() {
        this.id = new DocumentTermId();
        this.id.documentId = this.document.id;
        this.id.termId = this.term.getId();
    }

    public Integer getCount() {
        return this.count;
    }

    public Float getTfIdf() {
        return this.tfIdf;
    }
    
    public void setTfIdf(Float tfIdf) {
        this.tfIdf = tfIdf;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Term getTerm() {
        return term;
    }

    public int compareTo(DocumentTerm o) {
        return -1 * this.count.compareTo(o.count);
    }

    public String toString() {
        return this.getTerm().getValue() + " (" + this.getTerm().getPartOfSpeech() + ") : " + this.count;
    }

}
