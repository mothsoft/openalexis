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
package com.mothsoft.alexis.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public final class UserAuthenticationDetails extends org.springframework.security.core.userdetails.User {

    private static final long serialVersionUID = 1L;

    protected static final Set<GrantedAuthority> DEFAULT_AUTHORITIES;
    public static final Set<GrantedAuthority> ADMIN_AUTHORITIES;

    static {
        Set<GrantedAuthority> temp = new HashSet<GrantedAuthority>();
        temp.add(new GrantedAuthorityImpl("ROLE_USER"));
        DEFAULT_AUTHORITIES = Collections.unmodifiableSet(temp);

        Set<GrantedAuthority> adminTemp = new HashSet<GrantedAuthority>();
        adminTemp.addAll(DEFAULT_AUTHORITIES);
        adminTemp.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
        adminTemp.add(new GrantedAuthorityImpl("ROLE_ANALYSIS"));
        ADMIN_AUTHORITIES = Collections.unmodifiableSet(adminTemp);
    }

    private Long userId;
    private boolean admin;
    private boolean system;
    private String apiToken;

    public UserAuthenticationDetails(final User user) {
        super(user.getUsername(), user.getHashedPassword(), true, true, true, true, getAuthorities(user));

        this.userId = user.getId();
        this.admin = user.isAdmin();
        this.system = false;
        this.apiToken = user.getApiTokens().get(0).getToken();
    }

    private static Collection<GrantedAuthority> getAuthorities(final User user) {
        if (user.isAdmin()) {
            return ADMIN_AUTHORITIES;
        }

        final Set<GrantedAuthority> userAuthorities = new HashSet<GrantedAuthority>(DEFAULT_AUTHORITIES);

        if (user.isAnalysisRole()) {
            userAuthorities.add(new GrantedAuthorityImpl("ROLE_ANALYSIS"));
        }

        return userAuthorities;
    }

    public UserAuthenticationDetails(final UserAuthenticationDetails toCopy, final String apiToken) {
        super(toCopy.getUsername(), toCopy.getPassword(), true, true, true, true, getAuthorities(toCopy));

        this.userId = toCopy.getUserId();
        this.admin = toCopy.isAdmin();
        this.system = toCopy.isSystem();
        this.apiToken = apiToken;
    }

    private static Collection<GrantedAuthority> getAuthorities(final UserAuthenticationDetails user) {
        final Collection<GrantedAuthority> userAuthorities = new HashSet<GrantedAuthority>();
        userAuthorities.addAll(user.getAuthorities());
        return userAuthorities;
    }

    public UserAuthenticationDetails(final boolean systemAuthentication) {
        super("SYSTEM", "", true, true, true, true, ADMIN_AUTHORITIES);

        if (!systemAuthentication) {
            throw new IllegalArgumentException("This boolean is here to make sure you know what you're doing!");
        }

        this.userId = 0L;
        this.admin = true;
        this.system = true;
    }

    public Long getUserId() {
        return this.userId;
    }

    public boolean isAdmin() {
        return this.admin;
    }

    public boolean isSystem() {
        return this.system;
    }

    public String getApiToken() {
        return this.apiToken;
    }

}
