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
package com.mothsoft.alexis.rest.document.v1;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "ImportantTerms")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportantTerms {

    @XmlTransient
    private ArrayList<ImportantTerm> collection;

    public ImportantTerms() {
        super();
    }

    public ImportantTerms(Collection<ImportantTerm> collection) {
        this.collection = new ArrayList<ImportantTerm>(collection);
    }

    @XmlElement(name = "ImportantTerm")
    public ArrayList<ImportantTerm> getCollection() {
        return collection;
    }

    public void setCollection(ArrayList<ImportantTerm> collection) {
        this.collection = collection;
    }

}
