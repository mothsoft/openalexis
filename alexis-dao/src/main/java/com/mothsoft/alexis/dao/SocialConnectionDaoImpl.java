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

import com.mothsoft.alexis.domain.SocialConnection;
import com.mothsoft.alexis.domain.SocialNetworkType;

@Repository
public class SocialConnectionDaoImpl implements SocialConnectionDao {

    @PersistenceContext
    private EntityManager em;

    public void add(final SocialConnection socialConnection) {
        this.em.persist(socialConnection);
    }

    public void update(final SocialConnection socialConnection) {
        this.em.merge(socialConnection);
    }

    @Override
    public SocialConnection findByRemoteUsername(String username, SocialNetworkType socialNetworkType) {
        final Query query = this.em.createQuery("FROM SocialConnection sc WHERE sc.remoteUsername = :username "
                + "AND socialNetworkType = :socialNetworkType");
        query.setParameter("username", username);
        query.setParameter("socialNetworkType", socialNetworkType);

        @SuppressWarnings("unchecked")
        final List<SocialConnection> socialConnections = query.getResultList();

        if (!socialConnections.isEmpty()) {
            return socialConnections.get(0);
        }

        return null;
    }

}
