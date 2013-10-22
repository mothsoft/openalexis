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
package com.mothsoft.alexis.engine.textual;

import org.apache.commons.math3.linear.OpenMapRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SparseRealVector;

import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentAssociation;
import com.mothsoft.alexis.domain.DocumentNamedEntity;
import com.mothsoft.alexis.domain.DocumentTerm;

/**
 * Domain object to encapsulate features of a document and allow finding its
 * similarity with other documents through set operations
 * 
 */
public class DocumentFeatures {

    private final SparseRealVector termVector;
    private final SparseRealVector associationVector;
    private final SparseRealVector nameVector;

    public DocumentFeatures(final Document document, final DocumentFeatureContext context) {
        this.termVector = new OpenMapRealVector(Integer.MAX_VALUE);
        this.associationVector = new OpenMapRealVector(Integer.MAX_VALUE);
        this.nameVector = new OpenMapRealVector(Integer.MAX_VALUE);

        for (final DocumentAssociation association : document.getDocumentAssociations()) {
            final Integer id = context.getContextId(association);
            increment(associationVector, id, 1);
        }

        for (final DocumentTerm documentTerm : document.getDocumentTerms()) {
            final Integer termId = context.getContextId(documentTerm.getTerm());
            increment(termVector, termId, documentTerm.getCount());
        }

        for (final DocumentNamedEntity entity : document.getDocumentNamedEntities()) {
            final Integer id = context.getContextId(entity);
            increment(nameVector, id, 1);
        }
    }

    private void increment(RealVector vector, Integer id, int increment) {
        vector.addToEntry(id, increment);
    }

    public double cosineSimilarity(final DocumentFeatures other) {
        final double cosineTerms = cosine(this.termVector, other.termVector);
        final double cosineAssociations = cosine(this.associationVector, other.associationVector);
        final double cosineNames = cosine(this.nameVector, other.nameVector);

        // FIXME - more mathematical approach than equal parts?
        return (cosineTerms + cosineAssociations + cosineNames) / 3.0;
    }

    public double cosine(final RealVector v1, final RealVector v2) {
        final double norm = v1.getNorm();
        final double norm2 = v2.getNorm();
        final double divisor = norm * norm2;

        if (divisor == 0.0) {
            return 0;
        } else {
            return v1.dotProduct(v2) / (divisor);
        }
    }
}
