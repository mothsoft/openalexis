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
package com.mothsoft.alexis.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import com.mothsoft.alexis.domain.Source;

@Repository
public class SourceDaoImpl implements SourceDao {

    @PersistenceContext
    private EntityManager em;

    public void setEm(EntityManager em) {
        this.em = em;
    }

    public void add(Source source) {
        this.em.persist(source);
    }

    public Source get(Long id) {
        return this.em.find(Source.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<Source> list(Class<? extends Source> restrictionClass) {
        final String className = restrictionClass.getCanonicalName();
        final List<Source> sources = this.em.createQuery("FROM " + className + " ORDER BY id ASC").getResultList();
        return sources;
    }

    @SuppressWarnings("unchecked")
    public List<Source> listSourcesByOwner(Long userId, Class<? extends Source> restrictionClass) {
        final String className = restrictionClass.getCanonicalName();
        final List<Source> sources = this.em
                .createQuery("FROM " + className + " s WHERE s.userId = :userId ORDER BY id ASC")
                .setParameter("userId", userId).getResultList();
        return sources;
    }

    @SuppressWarnings("unchecked")
    public List<Source> listSourcesWithRetrievalDateMoreThanXMinutesAgo(int minutes,
            Class<? extends Source> restrictionClass) {
        final String className = restrictionClass.getCanonicalName();
        final Date now = new Date();
        final int minutesInMilliseconds = minutes * 60 * 1000;
        final Date minutesAgo = new Date(now.getTime() - minutesInMilliseconds);

        final List<Source> sources = this.em
                .createQuery(
                        "FROM " + className
                                + " s WHERE s.retrievalDate IS NULL OR s.retrievalDate < :minutesAgo ORDER BY id ASC")
                .setParameter("minutesAgo", minutesAgo).getResultList();
        return sources;
    }

    public void remove(final Source source) {
        this.em.remove(source);
    }

    public void update(final Source source) {
        this.em.merge(source);
    }

}
