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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity(name = "TopicDocument")
@Table(name = "topic_document")
public class TopicDocument {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "score")
    private Float score;

    @Column(name = "creation_date")
    private Date creationDate;

    @Version
    @Column(name = "version", columnDefinition = "smallint unsigned")
    protected Integer version;

    public TopicDocument() {
        // default constructor
    }

    public TopicDocument(final Topic topic, final Document document, final Float score) {
        this.topic = topic;
        this.document = document;
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public Topic getTopic() {
        return topic;
    }

    public Document getDocument() {
        return document;
    }

    public Float getScore() {
        return score;
    }

    public Date getCreationDate() {
        return this.creationDate;
    }

}
