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
import javax.persistence.PostLoad;
import javax.persistence.Table;

/**
 * A conceptually-related pair of terms in the context of a document
 * 
 * @author tgarrett
 * 
 */
@Entity(name = "DocumentAssociation")
@Table(name = "document_association")
public class DocumentAssociation {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne
    @JoinColumn(name = "term_a_id")
    private Term a;

    @ManyToOne
    @JoinColumn(name = "term_b_id")
    private Term b;

    @Column(name = "association_type", columnDefinition = "tinyint")
    private int intAssociationType;

    @Column(name = "association_count")
    private int associationCount;

    @Column(name = "association_weight")
    private float associationWeight;

    private transient AssociationType type;

    public DocumentAssociation(final Document document, final Term a, final Term b, AssociationType type, int count,
            float weight) {
        this.document = document;
        this.a = a;
        this.b = b;
        this.type = type;
        this.intAssociationType = this.type.getValue();
        this.associationWeight = weight;
    }

    public DocumentAssociation(final Term a, final Term b, AssociationType type) {
        this(null, a, b, type, 0, 0.0f);
    }

    protected DocumentAssociation() {
        // default constructor
    }

    @PostLoad
    protected void postLoad() {
        this.type = AssociationType.getByValue(this.intAssociationType);
    }

    public Long getId() {
        return this.id;
    }

    public Document getDocument() {
        return this.document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public AssociationType getAssociationType() {
        return this.type;
    }

    public int getAssociationCount() {
        return this.associationCount;
    }

    public void setAssociationCount(int associationCount) {
        this.associationCount = associationCount;
    }

    public float getAssociationWeight() {
        return this.associationWeight;
    }

    public void setAssociationWeight(float associationWeight) {
        this.associationWeight = associationWeight;
    }

    public Term getA() {
        return a;
    }

    public Term getB() {
        return b;
    }

    public final String toString() {
        return this.type + "(" + this.a.getValue() + "," + this.b.getValue() + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((a == null) ? 0 : a.hashCode());
        result = prime * result + ((b == null) ? 0 : b.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        DocumentAssociation other = (DocumentAssociation) obj;
        if (a == null) {
            if (other.a != null)
                return false;
        } else if (!a.equals(other.a))
            return false;
        if (b == null) {
            if (other.b != null)
                return false;
        } else if (!b.equals(other.b))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

}
