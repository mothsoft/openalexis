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

import java.util.HashMap;
import java.util.Map;

import com.mothsoft.alexis.domain.DocumentAssociation;
import com.mothsoft.alexis.domain.DocumentNamedEntity;
import com.mothsoft.alexis.domain.Term;

public class DocumentFeatureContext {

    private Map<String, Integer> associationMap;
    private Map<String, Integer> nameMap;

    public DocumentFeatureContext() {
        this.associationMap = new HashMap<String, Integer>();
        this.nameMap = new HashMap<String, Integer>();
    }

    public Integer getContextId(Term term) {
        return term.getId().intValue();
    }

    public Integer getContextId(DocumentAssociation association) {
        final String key = association.getA().getValueLowercase() + ":" + association.getB().getValueLowercase();

        if (associationMap.containsKey(key)) {
            return associationMap.get(key);
        } else {
            final int val = associationMap.size();
            associationMap.put(key, val);
            return val;
        }
    }

    public Integer getContextId(DocumentNamedEntity name) {
        final String key = name.getName();

        if (nameMap.containsKey(key)) {
            return nameMap.get(key);
        } else {
            final int val = nameMap.size();
            nameMap.put(key, val);
            return val;
        }
    }
}
