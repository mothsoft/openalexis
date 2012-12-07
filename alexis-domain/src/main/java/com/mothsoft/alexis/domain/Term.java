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
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

@Entity(name = "Term")
@Table(name = "term", uniqueConstraints = { @UniqueConstraint(columnNames = { "term_value", "part_of_speech" }) })
public class Term {

    @Id
    @Column(name = "id", columnDefinition = "INTEGER UNSIGNED")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "term_value")
    private String value;

    @Column(name = "term_value_lowercase")
    private String valueLowercase;

    @Column(name = "part_of_speech", columnDefinition = "tinyint")
    private byte partOfSpeechEnumValue;

    private transient PartOfSpeech partOfSpeech;

    @Version
    @Column(name = "version", columnDefinition = "tinyint unsigned")
    protected Integer version;

    public Term(final String value, final PartOfSpeech partOfSpeech) {
        this.value = value;
        this.partOfSpeech = partOfSpeech;
        this.partOfSpeechEnumValue = (byte) partOfSpeech.getValue();
    }

    protected Term() {
        // default constructor
    }

    @PrePersist
    protected void prePersist() {
        this.valueLowercase = this.value.toLowerCase();
    }

    @PostLoad
    protected void postLoad() {
        this.partOfSpeech = PartOfSpeech.getByValue((int) this.partOfSpeechEnumValue);
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public String getValueLowercase() {
        return this.valueLowercase;
    }

    public PartOfSpeech getPartOfSpeech() {
        return partOfSpeech;
    }

    public int getPartOfSpeechEnumValue() {
        return (int) partOfSpeechEnumValue;
    }

    public final String toString() {
        return getValue() + ":" + getPartOfSpeech().toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((partOfSpeech == null) ? 0 : partOfSpeech.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Term other = (Term) obj;
        if (partOfSpeech != other.partOfSpeech)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
