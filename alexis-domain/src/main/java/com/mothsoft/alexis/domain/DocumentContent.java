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

import java.io.UnsupportedEncodingException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TermVector;

@Entity(name = "DocumentContent")
@Table(name = "document_content")
public class DocumentContent {

    @Id
    @Column(name = "document_id")
    private Long id;

    @OneToOne(mappedBy = "documentContent")
    @ContainedIn
    private Document document;

    @Lob
    @Column(name = "text", columnDefinition = "mediumblob")
    @Field(name = "text", store = Store.NO, termVector = TermVector.YES, analyze = Analyze.YES)
    @FieldBridge(impl = ByteArrayAsStringFieldBridge.class)
    private byte[] text;

    public DocumentContent(final Document document, final String text) {
        this.document = document;
        this.document.setDocumentContent(this);
        setText(text);
    }

    protected DocumentContent() {
    }

    @PrePersist
    protected void prePersist() {
        this.id = this.document.getId();
    }

    Long getId() {
        return this.id;
    }

    public Document getDocument() {
        return this.document;
    }

    public String getText() {
        return new String(this.text);
    }

    public void setText(final String text) {
        try {
            this.text = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
