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

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
        setterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportantNamedEntity {

    public static final Comparator<ImportantNamedEntity> NAME_COMPARATOR = new Comparator<ImportantNamedEntity>() {
        @Override
        public int compare(ImportantNamedEntity arg0, ImportantNamedEntity arg1) {
            return arg0.getName().compareTo(arg1.getName());
        }
    };

    private String name;
    private Integer count;

    public ImportantNamedEntity(final String name, final Number count) {
        this.name = name;
        this.count = count.intValue();
    }

    protected ImportantNamedEntity() {
        // support frameworks
    }

    public String getName() {
        return this.name;
    }

    public Integer getCount() {
        return this.count;
    }

}
