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

import com.mothsoft.alexis.domain.DataSetType;

@Repository
public class DataSetTypeDaoImpl implements DataSetTypeDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void add(DataSetType type) {
        this.em.persist(type);
    }

    @Override
    public DataSetType findSystemDataSetType(String name) {
        final Query query = this.em.createQuery("FROM DataSetType WHERE name = :name AND system = TRUE");
        query.setParameter("name", name);

        @SuppressWarnings("unchecked")
        final List<DataSetType> types = query.getResultList();

        if (types.size() > 0) {
            return types.get(0);
        }

        return null;
    }

}
