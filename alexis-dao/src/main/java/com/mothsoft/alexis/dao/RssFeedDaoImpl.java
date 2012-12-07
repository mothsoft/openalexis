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
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.mothsoft.alexis.domain.RssFeed;

@Repository
public class RssFeedDaoImpl implements RssFeedDao {

    @PersistenceContext
    private EntityManager em;

    public void setEm(final EntityManager em) {
        this.em = em;
    }

    public void add(final RssFeed rssFeed) {
        this.em.persist(rssFeed);
    }

    public RssFeed findByUrl(String url) {
        final Query query = this.em.createQuery("FROM RssFeed WHERE url = :url");
        query.setParameter("url", url);

        @SuppressWarnings("unchecked")
        final List<RssFeed> results = query.getResultList();

        if (results.size() == 1) {
            return results.get(0);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public List<RssFeed> listRssFeedsWithRetrievalDateMoreThanXMinutesAgo(int minutes) {
        final Date now = new Date();
        final int minutesInMilliseconds = minutes * 60 * 1000;
        final Date minutesAgo = new Date(now.getTime() - minutesInMilliseconds);

        final List<RssFeed> feeds = this.em
                .createQuery(
                        "FROM RssFeed f WHERE f.retrievalDate IS NULL OR f.retrievalDate < :minutesAgo ORDER BY id ASC")
                .setParameter("minutesAgo", minutesAgo).getResultList();
        return feeds;
    }

    public void remove(final RssFeed rssFeed) {
        this.em.remove(rssFeed);
    }

    public void update(final RssFeed rssFeed) {
        this.em.merge(rssFeed);
    }

}
