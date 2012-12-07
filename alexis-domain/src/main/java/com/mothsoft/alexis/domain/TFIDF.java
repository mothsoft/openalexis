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

import org.apache.log4j.Logger;
import org.apache.lucene.search.DefaultSimilarity;

public class TFIDF {

    private static final Logger logger = Logger.getLogger(TFIDF.class);

    private static final DefaultSimilarity SIMILARITY = new DefaultSimilarity();

    public static Float score(String term, int termCount, int documentSize, int totalNumberOfDocuments,
            int numberOfDocumentsContainingTerm) {

        if (numberOfDocumentsContainingTerm == 0) {
            return 0.0f;
        }

        float tf = SIMILARITY.tf((float) termCount / (float) documentSize);
        float idf = SIMILARITY.idf(numberOfDocumentsContainingTerm, totalNumberOfDocuments);
        float tfIdf = tf * idf;

        if (logger.isTraceEnabled()) {
            logger.trace("calculateScore('" + term + "', " + termCount + ", " + documentSize + ", "
                    + totalNumberOfDocuments + ", " + numberOfDocumentsContainingTerm + ") = " + tfIdf);
        }

        return tfIdf;
    }

}
