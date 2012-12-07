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

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.mothsoft.alexis.dao.UserDao;
import com.mothsoft.alexis.domain.UserAuthenticationDetails;

public class AlexisApiAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private UserDetailsService userDetailsService;
    private UserDao userDao;
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;

    public AlexisApiAuthenticationProvider(UserDetailsService userDetailsService, UserDao userDao,
            PlatformTransactionManager transactionManager) {
        this.userDetailsService = userDetailsService;
        this.userDao = userDao;
        this.transactionManager = transactionManager;
        this.transactionTemplate = new TransactionTemplate(this.transactionManager);
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken token)
            throws AuthenticationException {
    }

    @Override
    protected UserDetails retrieveUser(final String username, final UsernamePasswordAuthenticationToken token)
            throws AuthenticationException {
        return this.transactionTemplate.execute(new TransactionCallback<UserDetails>() {

            @Override
            public UserDetails doInTransaction(TransactionStatus arg0) {
                final String apiToken = String.valueOf(token.getCredentials());
                final boolean valid = AlexisApiAuthenticationProvider.this.userDao.authenticate(username, apiToken);

                if (!valid) {
                    throw new BadCredentialsException(username);
                }

                final UserDetails userDetails = AlexisApiAuthenticationProvider.this.userDetailsService
                        .loadUserByUsername(username);
                final UserDetails toReturn = new UserAuthenticationDetails((UserAuthenticationDetails) userDetails,
                        apiToken);
                return toReturn;
            }
        });

    }
}
