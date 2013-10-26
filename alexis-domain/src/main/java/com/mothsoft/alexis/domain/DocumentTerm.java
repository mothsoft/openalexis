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

public class DocumentTerm implements Comparable<DocumentTerm> {

    @JsonIgnore
    private String documentId;

    private Term term;

    private Integer count;

    private Float tfIdf;

    public DocumentTerm(final String documentId, final Term term, final Integer count) {
        this.documentId = documentId;
        this.term = term;
        this.count = count;
    }

    protected DocumentTerm() {
        super();
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

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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
