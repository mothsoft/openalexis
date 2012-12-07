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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity(name = "Source")
@Table(name = "source")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Source {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "retrieval_date")
    private Date retrievalDate;

    public Source(final Long userId) {
        this.userId = userId;
    }

    protected Source() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    public Date getRetrievalDate() {
        return retrievalDate;
    }

    public void setRetrievalDate(Date retrievalDate) {
        this.retrievalDate = retrievalDate;
    }

    public abstract SourceType getSourceType();

    public abstract String getDescription();

}

class SourceTypeIdResolver implements TypeIdResolver {

    private Map<String, JavaType> types;

    @Override
    public org.codehaus.jackson.annotate.JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    @Override
    public String idFromValue(Object object) {
        final Source source = (Source) object;
        return source.getSourceType().name();
    }

    @Override
    public void init(JavaType type) {
        this.types = new HashMap<String, JavaType>();

        this.types.put(SourceType.ALL.name(), TypeFactory.fromCanonical(Source.class.getCanonicalName()));
        this.types.put(SourceType.R.name(), TypeFactory.fromCanonical(RssSource.class.getCanonicalName()));
        this.types.put(SourceType.T.name(), TypeFactory.fromCanonical(TwitterSource.class.getCanonicalName()));
    }

    @Override
    public JavaType typeFromId(String id) {
        return types.get(id);
    }

}
