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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "SocialConnection")
@Table(name = "social_connection")
public class SocialConnection {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "remote_username")
    private String remoteUsername;

    @Column(name = "oauth_token")
    private String oauthToken;

    @Column(name = "oauth_token_secret")
    private String oauthTokenSecret;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_network_type", columnDefinition = "char(1)")
    private SocialNetworkType socialNetworkType;

    public SocialConnection(final User user, final String remoteUsername, final String oauthToken,
            final String oauthTokenSecret, final SocialNetworkType socialNetworkType) {
        this.user = user;
        this.remoteUsername = remoteUsername;
        this.oauthToken = oauthToken;
        this.oauthTokenSecret = oauthTokenSecret;
        this.socialNetworkType = socialNetworkType;
    }

    protected SocialConnection() {

    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getRemoteUsername() {
        return this.remoteUsername;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    public String getOauthTokenSecret() {
        return oauthTokenSecret;
    }

    public void setOauthTokenSecret(String oauthTokenSecret) {
        this.oauthTokenSecret = oauthTokenSecret;
    }

    public SocialNetworkType getSocialNetworkType() {
        return socialNetworkType;
    }

}
