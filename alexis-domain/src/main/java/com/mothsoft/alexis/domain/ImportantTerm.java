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

import java.util.Comparator;

public class ImportantTerm {

    public static final Comparator<ImportantTerm> NAME_COMPARATOR = new Comparator<ImportantTerm>() {
        @Override
        public int compare(ImportantTerm arg0, ImportantTerm arg1) {
            return arg0.getTermValue().compareTo(arg1.getTermValue());
        }
    };

    private Long documentId;
    private Long termId;
    private String termValue;
    private Float tfIdf;
    private Integer count;
    private Integer percentageOfMaximum;

    public ImportantTerm(String termValue, Integer count, Float tfIdf, Float maxTfIdfInSet) {
        this.termValue = termValue;
        this.count = count;
        this.tfIdf = tfIdf;
        this.percentageOfMaximum = (int) (100 * (tfIdf / maxTfIdfInSet));
    }

    public ImportantTerm(Long documentId, Long termId, String termValue, Float tfIdf, Integer count,
            Double maxTfIdfInSet) {
        super();
        this.documentId = documentId;
        this.termId = termId;
        this.termValue = termValue;
        this.tfIdf = tfIdf;
        this.count = count;
        this.percentageOfMaximum = (int) (100 * (tfIdf / maxTfIdfInSet));
    }

    public Long getDocumentId() {
        return documentId;
    }

    public Long getTermId() {
        return termId;
    }

    public String getTermValue() {
        return termValue;
    }

    public Float getTfIdf() {
        return tfIdf;
    }

    public Integer getCount() {
        return count;
    }

    public Integer getPercentageOfMaximum() {
        return percentageOfMaximum;
    }

}
