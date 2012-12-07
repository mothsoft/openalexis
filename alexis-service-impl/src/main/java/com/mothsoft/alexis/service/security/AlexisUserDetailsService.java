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
package com.mothsoft.alexis.service.security;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.dao.UserDao;
import com.mothsoft.alexis.domain.UserAuthenticationDetails;

public class AlexisUserDetailsService implements UserDetailsService {

    private UserDao dao;

    public AlexisUserDetailsService(final UserDao dao) {
        this.dao = dao;
    }

    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        final com.mothsoft.alexis.domain.User user = this.dao.findUserByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("username");
        }

        return new UserAuthenticationDetails(user);
    }

}
