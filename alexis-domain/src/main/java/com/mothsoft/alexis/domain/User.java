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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity(name = "User")
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue
    Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "hashed_password", length = 64, nullable = false, columnDefinition = "char(64)")
    private String hashedPassword;

    @Column(name = "salt", length = 64, nullable = false, columnDefinition = "char(64)")
    private String passwordSalt;

    @Column(name = "is_admin", columnDefinition = "bit")
    private boolean admin;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private List<SocialConnection> socialConnections;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "user")
    private List<UserApiToken> apiTokens;

    public User() {
        this.socialConnections = new ArrayList<SocialConnection>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public boolean isAdmin() {
        return this.admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public List<SocialConnection> getSocialConnections() {
        return this.socialConnections;
    }

    public List<SocialConnection> getSocialConnectionsByType(final SocialNetworkType socialNetworkType) {
        final List<SocialConnection> connections = new ArrayList<SocialConnection>();

        for (final SocialConnection ith : getSocialConnections()) {
            if (socialNetworkType.equals(ith.getSocialNetworkType())) {
                connections.add(ith);
            }
        }
        return connections;
    }

    public List<UserApiToken> getApiTokens() {
        return this.apiTokens;
    }

}
