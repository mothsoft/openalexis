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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.mothsoft.alexis.domain.RssFeed;
import com.mothsoft.alexis.domain.RssSource;
import com.mothsoft.alexis.domain.SocialConnection;
import com.mothsoft.alexis.domain.SocialNetworkType;
import com.mothsoft.alexis.domain.SourceType;
import com.mothsoft.alexis.domain.User;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.SourceService;
import com.mothsoft.alexis.service.UserService;

public class AddEditSourceBackingBean {

    private SourceService sourceService;
    private UserService userService;

    private User user;
    private List<SocialConnection> twitterProfiles;
    private SourceType sourceType;
    private String url;

    public AddEditSourceBackingBean() {
    }

    public void setSourceService(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

    public boolean isTwitter() {
        return SourceType.T == this.sourceType;
    }

    public boolean isRss() {
        return SourceType.R == this.sourceType;
    }

    public String getType() {
        final String value;

        switch (this.sourceType) {
        case T:
            value = "twitter";
            break;
        // case FACEBOOK:
        // value = "facebook";
        // break;
        case R:
            value = "rss";
            break;
        default:
            value = null;
            break;
        }

        return value;
    }

    public void setType(final String type) {
        if ("twitter".equals(type)) {
            this.sourceType = SourceType.T;
        } else if ("rss".equals(type)) {
            this.sourceType = SourceType.R;
        }/*
          * else if ("facebook".equals(type)) { this.sourceType =
          * SourceType.FACEBOOK; }
          */
        else {
            throw new IllegalArgumentException();
        }
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void remove(final ActionEvent event) {
        final Long id = (Long) event.getComponent().getAttributes().get("sourceId");
        this.sourceService.remove(id);
    }

    public void saveRss(final ActionEvent event) {
        final RssFeed feed = this.sourceService.findOrCreateRssFeed(this.url);
        final RssSource source = new RssSource(feed, CurrentUserUtil.getCurrentUserId());
        this.sourceService.add(source);
    }

    public void validateUrl(final FacesContext context, final UIComponent component, final Object value) {
        final String urlString = (String) value;

        try {
            new URL(urlString);
        } catch (MalformedURLException e) {
            final UIInput input = (UIInput) component;
            input.setValid(false);

            final FacesMessage msg = new FacesMessage("URL is invalid");
            context.addMessage(component.getClientId(context), msg);
        }
    }

    public Integer getNumberOfTwitterProfiles() {
        return getTwitterProfiles().size();
    }

    public List<SocialConnection> getTwitterProfiles() {
        if (this.twitterProfiles == null) {
            final Long userId = CurrentUserUtil.getCurrentUserId();
            this.user = this.userService.getUser(userId);
            this.twitterProfiles = user.getSocialConnectionsByType(SocialNetworkType.T);
        }
        return this.twitterProfiles;
    }

}
