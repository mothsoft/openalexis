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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.search.annotations.ContainedIn;

@Entity(name = "DocumentUser")
@Table(name = "document_user")
public class DocumentUser {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    @ContainedIn
    private Document document;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public DocumentUser() {
        // default constructor
    }

    public DocumentUser(final Document document, final User user) {
        this.document = document;
        this.user = user;
    }

    public Long getId() {
        return this.id;
    }

    public Document getDocument() {
        return document;
    }

    public User getUser() {
        return user;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((document == null || document.id == null) ? 0 : document.id.hashCode());
        result = prime * result + ((user == null || user.id == null) ? 0 : user.id.hashCode());
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
        DocumentUser other = (DocumentUser) obj;
        if (document == null) {
            if (other.document != null)
                return false;
        } else if (!document.id.equals(other.document.id))
            return false;
        if (user == null) {
            if (other.user != null)
                return false;
        } else if (!user.id.equals(other.user.id))
            return false;
        return true;
    }

}
