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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.domain.DataSetType;
import com.mothsoft.alexis.domain.TopicActivityDataSet;

@Repository
public class DataSetDaoImpl implements DataSetDao {

    @PersistenceContext
    private EntityManager em;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.mothsoft.alexis.dao.DataSetDao#add(com.mothsoft.alexis.domain.DataSet
     * )
     */
    @Override
    public void add(DataSet set) {
        this.em.persist(set);
    }

    @Override
    public DataSet findAggregateTopicActivityDataSet(Long userId) {
        final Query query = this.em
                .createQuery("FROM DataSet ds WHERE ds.userId = :userId AND ds.aggregate = true AND ds.name = '*All Topics*'");
        query.setParameter("userId", userId);

        @SuppressWarnings("unchecked")
        final List<DataSet> dataSets = query.getResultList();

        if (dataSets == null || dataSets.isEmpty()) {
            return null;
        }
        return (DataSet) dataSets.get(0);
    }

    @Override
    public List<TopicActivityDataSet> findMostActiveTopicDataSets(Long userId, Timestamp startDate, Timestamp endDate,
            int limit) {
        final Query query = this.em
                .createQuery("SELECT tads, SUM(pt.y) FROM TopicActivityDataSet tads LEFT JOIN tads.points pt "
                        + " WHERE tads.userId = :userId AND pt.x >= :startDate "
                        + "       AND pt.x <= :endDate AND tads.aggregate = false                 "
                        + "       AND pt.y > 0.0                                                  "
                        + " GROUP BY tads.id HAVING SUM(pt.y) > 0 ORDER BY SUM(pt.y) DESC ");
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setMaxResults(limit);

        final List<?> objects = query.getResultList();
        final List<TopicActivityDataSet> dataSets = new ArrayList<TopicActivityDataSet>(limit);

        for (final Object ith : objects) {
            final Object[] array = (Object[]) ith;
            dataSets.add((TopicActivityDataSet) array[0]);
        }

        return dataSets;
    }

    @Override
    public DataSet findSystemDataSet(DataSetType type, String name) {
        final Query query = this.em.createQuery("FROM DataSet ds WHERE ds.userId IS NULL AND ds.name = :name");
        query.setParameter("name", name);
        @SuppressWarnings("unchecked")
        List<DataSet> dataSets = query.getResultList();

        if (dataSets.isEmpty()) {
            return null;
        } else if (dataSets.size() > 1) {
            throw new IllegalStateException("Expecting 0 or 1 data sets, found: " + dataSets.size());
        } else {
            return dataSets.get(0);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TopicActivityDataSet> findTopicActivityDataSetsByUser(Long userId) {
        final Query query = this.em
                .createQuery("FROM TopicActivityDataSet tads WHERE tads.userId = :userId AND tads.aggregate = false");
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    @Override
    public TopicActivityDataSet findTopicActivityDataSet(Long topicId) {
        final Query query = this.em.createQuery("FROM TopicActivityDataSet tads WHERE tads.topic.id = :topicId");
        query.setParameter("topicId", topicId);

        final List<?> results = query.getResultList();

        if (results == null || results.isEmpty()) {
            return null;
        }

        return (TopicActivityDataSet) results.get(0);
    }

    @Override
    public DataSet get(Long id) {
        return this.em.find(DataSet.class, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.mothsoft.alexis.dao.DataSetDao#list(java.lang.Long)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<DataSet> list(Long userId) {
        final Query query = this.em
                .createQuery("FROM DataSet WHERE userId = :userId OR userId IS NULL ORDER BY type.id ASC, aggregate, name");
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.mothsoft.alexis.dao.DataSetDao#remove(com.mothsoft.alexis.domain.
     * DataSet)
     */
    @Override
    public void remove(DataSet set) {
        this.em.remove(set);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.mothsoft.alexis.dao.DataSetDao#update(com.mothsoft.alexis.domain.
     * DataSet)
     */
    @Override
    public void update(DataSet set) {
        this.em.merge(set);
    }

}
