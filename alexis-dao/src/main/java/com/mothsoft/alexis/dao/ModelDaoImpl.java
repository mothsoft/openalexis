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

import com.mothsoft.alexis.domain.DataRange;
import com.mothsoft.alexis.domain.Model;
import com.mothsoft.alexis.domain.ModelState;
import com.mothsoft.alexis.domain.ModelType;

@Repository
public class ModelDaoImpl implements ModelDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void add(Model model) {
        this.em.persist(model);
    }

    @Override
    public List<Model> findByTypeAndState(ModelType type, ModelState state) {
        final Query query = this.em
                .createQuery("SELECT m FROM Model m WHERE m.state = :state AND m.type = :type ORDER BY m.id ASC");
        query.setParameter("type", type);
        query.setParameter("state", state);

        @SuppressWarnings("unchecked")
        final List<Model> models = (List<Model>) query.getResultList();

        return models;
    }

    @Override
    public Model findByUserAndName(Long userId, String name) {
        final Query query = this.em.createQuery("SELECT m FROM Model m WHERE userId = :userId AND name = :name");
        query.setParameter("userId", userId);
        query.setParameter("name", name);

        @SuppressWarnings("unchecked")
        final List<Model> models = (List<Model>) query.getResultList();

        if (models == null || models.size() != 1) {
            return null;
        } else {
            return models.get(0);
        }
    }

    @Override
    public Long findAndMarkOne(ModelState current, ModelState toSet) {
        final Query query = this.em.createQuery("SELECT m FROM Model m WHERE m.state =:state ORDER BY m.id ASC");
        query.setParameter("state", current);
        query.setMaxResults(1);

        @SuppressWarnings("unchecked")
        final List<Model> models = (List<Model>) query.getResultList();

        if (models == null || models.isEmpty()) {
            return null;
        } else {
            final Model model = models.get(0);
            model.setState(toSet);
            return model.getId();
        }
    }

    @Override
    public Model get(Long id) {
        return this.em.find(Model.class, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataRange<Model> listByOwner(Long userId, int first, int count) {
        final Query query = this.em
                .createQuery("SELECT m FROM Model m JOIN m.topic t WHERE t.userId = :userId ORDER BY m.name ASC");
        query.setParameter("userId", userId);
        query.setFirstResult(first);
        query.setMaxResults(count);

        final Query query2 = this.em
                .createQuery("SELECT COUNT(m.id) FROM Model m JOIN m.topic t WHERE t.userId = :userId");
        query2.setParameter("userId", userId);
        int total = ((Number) query2.getSingleResult()).intValue();

        final List<Model> list = query.getResultList();
        final DataRange<Model> range = new DataRange<Model>(list, first, total);
        return range;
    }

    @Override
    public void remove(Model model) {
        this.em.remove(model);
        this.em.remove(model.getPredictionDataSet());
    }

}
