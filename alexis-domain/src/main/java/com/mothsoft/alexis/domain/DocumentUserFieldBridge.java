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

import java.util.List;

import org.hibernate.search.bridge.LuceneOptions;

public class DocumentUserFieldBridge implements org.hibernate.search.bridge.FieldBridge {

    public DocumentUserFieldBridge() {
    }

    public void set(String name, Object value, org.apache.lucene.document.Document luceneDocument,
            LuceneOptions luceneOptions) {
        luceneDocument.removeFields(name);

        @SuppressWarnings("unchecked")
        final List<DocumentUser> documentUsers = (List<DocumentUser>) value;
        for (final DocumentUser docUser : documentUsers) {
            final Long userId = docUser.getUser().getId();
            luceneOptions.addNumericFieldToDocument(name, userId, luceneDocument);
        }
    }

}