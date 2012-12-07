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
package com.mothsoft.alexis.web;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.mothsoft.alexis.domain.SocialConnection;
import com.mothsoft.alexis.domain.SocialNetworkType;
import com.mothsoft.alexis.domain.TwitterSource;
import com.mothsoft.alexis.domain.User;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.SourceService;
import com.mothsoft.alexis.service.UserService;
import com.mothsoft.integration.twitter.TwitterService;

public class TwitterBackingBean {

    // dependency
    private SourceService sourceService;
    private TwitterService twitterService;
    private UserService userService;

    // state
    private RequestToken requestToken;
    private String denied;
    private String oauthToken;
    private String oauthVerifier;

    public TwitterBackingBean() {
    }

    public void setSourceService(final SourceService sourceService) {
        this.sourceService = sourceService;
    }

    public void setTwitterService(final TwitterService twitterService) {
        this.twitterService = twitterService;
    }

    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

    public String getDenied() {
        return denied;
    }

    public void setDenied(String denied) {
        this.denied = denied;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    public String getOauthVerifier() {
        return oauthVerifier;
    }

    public void setOauthVerifier(String oauthVerifier) {
        this.oauthVerifier = oauthVerifier;
    }

    public void requestOauthAuthorization(final ActionEvent event) throws IOException {
        this.requestToken = this.twitterService.getRequestToken();
        FacesContext.getCurrentInstance().getExternalContext().redirect(requestToken.getAuthorizationURL());
    }

    public String oauthCallback() {
        if (this.oauthToken != null && this.oauthVerifier != null && this.requestToken != null) {

            final AccessToken accessToken = this.twitterService.getAccessToken(this.requestToken, this.oauthVerifier);

            final User user = this.userService.getUser(CurrentUserUtil.getCurrentUserId());
            final SocialConnection socialConnection = new SocialConnection(user, accessToken.getScreenName(),
                    accessToken.getToken(), accessToken.getTokenSecret(), SocialNetworkType.T);

            // FIXME - dupe logic and stuff
            this.userService.addOrUpdateSocialConnection(user, socialConnection);

            final TwitterSource twitterSource = new TwitterSource(socialConnection);
            twitterSource.setUserId(user.getId());
            this.sourceService.add(twitterSource);
        }

        return "/sources/list?faces-redirect=true";
    }
}
