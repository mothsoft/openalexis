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

public class ParsedContent {

    private final Collection<DocumentAssociation> documentAssociations;
    private final Collection<DocumentTerm> documentTerms;
    private final Collection<DocumentNamedEntity> documentNamedEntities;
    private final Integer documentTermCount;

    public ParsedContent(final Collection<DocumentAssociation> documentAssociations,
            final Collection<DocumentTerm> documentTerms, final List<DocumentNamedEntity> documentNamedEntities,
            final Integer documentTermCount) {
        this.documentAssociations = documentAssociations;
        this.documentTerms = documentTerms;
        this.documentNamedEntities = documentNamedEntities;
        this.documentTermCount = documentTermCount;
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

}
