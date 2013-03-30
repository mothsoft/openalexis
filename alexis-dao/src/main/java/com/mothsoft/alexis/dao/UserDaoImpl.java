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
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.mothsoft.alexis.domain.DateConstants;
import com.mothsoft.alexis.domain.User;
import com.mothsoft.alexis.domain.UserApiToken;

@Repository
public class UserDaoImpl implements UserDao {

    private static final Logger logger = Logger.getLogger(UserDaoImpl.class);

    @PersistenceContext
    private EntityManager em;

    public void setEm(EntityManager em) {
        this.em = em;
    }

    public void add(final User user) {
        this.em.persist(user);
    }
    
    public User get(final Long id) {
        return this.em.find(User.class, id);
    }

    public User findUserByUsername(String username) {
        @SuppressWarnings("unchecked")
        final List<User> users = this.em.createQuery("from User u where u.username = :username")
                .setParameter("username", username).getResultList();
        if (users == null || users.size() != 1) {
            return null;
        }
        return users.get(0);
    }

    public void update(User user) {
        this.em.merge(user);
    }

    @Override
    public UserApiToken createApiToken(User user) {
        purgeStaleTokens(user, DateConstants.ONE_HOUR_IN_MILLISECONDS);
        final UserApiToken token = new UserApiToken(user, UUID.randomUUID().toString());
        user.getApiTokens().add(token);
        this.em.persist(token);
        return token;
    }

    private void purgeStaleTokens(User user, Long millisAgeAllowed) {
        final Date oldestPermissible = new Date(System.currentTimeMillis() - millisAgeAllowed);
        final Query query = this.em
                .createQuery("DELETE FROM UserApiToken token WHERE token.user.id = :userId AND token.lastUsed < :oldestDate");
        query.setParameter("userId", user.getId());
        query.setParameter("oldestDate", oldestPermissible);
        int purged = query.executeUpdate();

        logger.debug("Purged " + purged + " stale API tokens for user: " + user.getUsername());
    }

    @Override
    public boolean authenticate(String username, String token) {
        final Query query = this.em
                .createQuery("SELECT token FROM UserApiToken token JOIN token.user user WHERE user.username = :username AND token.token = :token");
        query.setParameter("username", username);
        query.setParameter("token", token);

        final List<UserApiToken> tokens = query.getResultList();
        final boolean valid = tokens.size() == 1;

        if (valid) {
            tokens.get(0).used();
        }

        return valid;
    }

    @Override
    public void invalidateApiToken(UserApiToken token) {
        token.getUser().getApiTokens().remove(token);
        this.em.remove(token);
    }

}
