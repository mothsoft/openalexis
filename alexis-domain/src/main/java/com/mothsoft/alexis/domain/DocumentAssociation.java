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

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * A conceptually-related pair of terms in the context of a document
 * 
 * @author tgarrett
 * 
 */
public class DocumentAssociation {

    @JsonIgnore
    private String documentId;

    private Term a;

    private Term b;

    private int count;

    private AssociationType type;

    public DocumentAssociation(final String documentId, final Term a, final Term b, AssociationType type, int count) {
        this.documentId = documentId;
        this.a = a;
        this.b = b;
        this.count = count;
    }

    public DocumentAssociation(final Term a, final Term b, AssociationType type) {
        this(null, a, b, type, 0);
    }

    protected DocumentAssociation() {
        // default constructor
    }

    public String getDocumentId() {
        return this.documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public AssociationType getType() {
        return type;
    }

    public void setType(AssociationType type) {
        this.type = type;
    }

    public void setA(Term a) {
        this.a = a;
    }

    public void setB(Term b) {
        this.b = b;
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
