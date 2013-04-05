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
package com.mothsoft.alexis.service.impl;

import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.dao.SocialConnectionDao;
import com.mothsoft.alexis.dao.UserDao;
import com.mothsoft.alexis.domain.SocialConnection;
import com.mothsoft.alexis.domain.SocialNetworkType;
import com.mothsoft.alexis.domain.User;
import com.mothsoft.alexis.domain.UserApiToken;
import com.mothsoft.alexis.service.UserService;

@Transactional
public class UserServiceImpl implements UserService {

    private SocialConnectionDao socialConnectionDao;
    private UserDao userDao;

    public void setSocialConnectionDao(final SocialConnectionDao socialConnectionDao) {
        this.socialConnectionDao = socialConnectionDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void addUser(final User user) {
        this.userDao.add(user);
    }

    public User getUser(final Long userId) {
        return this.userDao.get(userId);
    }

    public void update(User user) {
        this.userDao.update(user);
    }

    public void addOrUpdateSocialConnection(final User user, final SocialConnection socialConnection) {
        // FIXME - dupe/merge checks

        this.socialConnectionDao.add(socialConnection);

        user.getSocialConnections().add(socialConnection);
        update(user);
    }

    @Override
    public SocialConnection findSocialConnectionByRemoteUsername(String username, SocialNetworkType networkType) {
        return this.socialConnectionDao.findByRemoteUsername(username, networkType);
    }

    @Override
    public UserApiToken createApiToken(User user) {
        return this.userDao.createApiToken(user);
    }

}
