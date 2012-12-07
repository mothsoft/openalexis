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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "DocumentNamedEntity")
@Table(name = "document_named_entity")
public class DocumentNamedEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "count", columnDefinition = "smallint unsigned")
    private Integer count;

    public DocumentNamedEntity() {
        super();
    }

    public DocumentNamedEntity(final String name, final Integer count) {
        this.name = name;
        this.count = count;
    }

    public Document getDocument() {
        return document;
    }

    public String getName() {
        return name;
    }

    public Integer getCount() {
        return count;
    }

    public Long getId() {
        return id;
    }

    protected void setDocument(Document document) {
        this.document = document;
    }

}
