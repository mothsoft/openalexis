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


/**
 * A holder of a document and its score together. A score will have contextual
 * meaning based on what operation retrieves the DocumentScore object
 * 
 * @author tgarrett
 * 
 */
public class DocumentScore {

    private Document document;
    private Float score;

    public DocumentScore(final Document document, final Float score) {
        this.document = document;
        this.score = score;

    }

    public Document getDocument() {
        return this.document;
    }

    public Float getScore() {
        return this.score;
    }
}
