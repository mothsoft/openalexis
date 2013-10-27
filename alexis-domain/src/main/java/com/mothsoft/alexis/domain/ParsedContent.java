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

import java.util.Collection;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedContent {

    @JsonProperty("documentId")
    private String documentId;

    @JsonProperty("associations")
    private Collection<DocumentAssociation> documentAssociations;

    @JsonProperty("terms")
    private Collection<DocumentTerm> documentTerms;

    @JsonProperty("names")
    private Collection<DocumentNamedEntity> documentNamedEntities;

    @JsonProperty("termCount")
    private Integer documentTermCount;

    public ParsedContent(final String documentId, final Collection<DocumentAssociation> documentAssociations,
            final Collection<DocumentTerm> documentTerms, final List<DocumentNamedEntity> documentNamedEntities,
            final Integer documentTermCount) {
        this.documentId = documentId;
        this.documentAssociations = documentAssociations;
        this.documentTerms = documentTerms;
        this.documentNamedEntities = documentNamedEntities;
        this.documentTermCount = documentTermCount;
    }

    protected ParsedContent() {
        // support framework initialization
    }

    public final Collection<DocumentAssociation> getDocumentAssociations() {
        return this.documentAssociations;
    }

    public final Collection<DocumentTerm> getTerms() {
        return this.documentTerms;
    }

    public final Collection<DocumentNamedEntity> getNamedEntities() {
        return this.documentNamedEntities;
    }

    public final Integer getDocumentTermCount() {
        return this.documentTermCount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((documentAssociations == null) ? 0 : documentAssociations.hashCode());
        result = prime * result + ((documentId == null) ? 0 : documentId.hashCode());
        result = prime * result + ((documentNamedEntities == null) ? 0 : documentNamedEntities.hashCode());
        result = prime * result + ((documentTermCount == null) ? 0 : documentTermCount.hashCode());
        result = prime * result + ((documentTerms == null) ? 0 : documentTerms.hashCode());
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
        ParsedContent other = (ParsedContent) obj;
        if (documentAssociations == null) {
            if (other.documentAssociations != null)
                return false;
        } else if (!documentAssociations.equals(other.documentAssociations))
            return false;
        if (documentId == null) {
            if (other.documentId != null)
                return false;
        } else if (!documentId.equals(other.documentId))
            return false;
        if (documentNamedEntities == null) {
            if (other.documentNamedEntities != null)
                return false;
        } else if (!documentNamedEntities.equals(other.documentNamedEntities))
            return false;
        if (documentTermCount == null) {
            if (other.documentTermCount != null)
                return false;
        } else if (!documentTermCount.equals(other.documentTermCount))
            return false;
        if (documentTerms == null) {
            if (other.documentTerms != null)
                return false;
        } else if (!documentTerms.equals(other.documentTerms))
            return false;
        return true;
    }

}
