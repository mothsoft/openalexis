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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.mothsoft.alexis.domain.Topic;
import com.mothsoft.alexis.domain.TopicDocument;

@Repository
public class TopicDaoImpl implements TopicDao {

    @PersistenceContext
    private EntityManager em;

    public void setEm(EntityManager em) {
        this.em = em;
    }

    public void add(final Topic topic) {
        this.em.persist(topic);
    }

    public void add(final TopicDocument topicDocument) {
        this.em.persist(topicDocument);
    }

    public Topic findTopicByUserAndName(Long userId, String name) {
        final Query query = this.em.createQuery("select t from Topic t where t.userId = :userId and t.name = :name");
        query.setParameter("userId", userId);
        query.setParameter("name", name);

        @SuppressWarnings("unchecked")
        final List<Topic> topics = query.getResultList();

        if (topics.isEmpty()) {
            return null;
        } else {
            return topics.get(0);
        }
    }

    public Topic get(final Long id) {
        return this.em.find(Topic.class, id);
    }

    public List<Topic> list() {
        @SuppressWarnings("unchecked")
        final List<Topic> topics = (List<Topic>) this.em.createQuery("from Topic t ORDER BY t.id ASC").getResultList();
        return topics;
    }

    public List<Topic> listTopicsByOwner(Long userId) {
        @SuppressWarnings("unchecked")
        final List<Topic> topics = this.em.createQuery("from Topic t where t.userId = :userId ORDER BY t.name ASC")
                .setParameter("userId", userId).getResultList();
        return topics;
    }

    public void remove(final Topic topic) {
        this.em.remove(topic);
    }

    public void update(final Topic topic) {
        this.em.merge(topic);
    }

}
