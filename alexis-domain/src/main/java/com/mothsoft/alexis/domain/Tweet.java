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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Tweet extends Document {

    private Long remoteTweetId;

    private String screenName;

    private String fullName;

    private String profileImageUrl;

    private transient String title;

    private boolean retweet;

    private String retweetUserName;

    private List<TweetLink> links;

    private List<TweetMention> mentions;

    private List<TweetHashtag> hashtags;

    public Tweet(Long remoteTweetId, Date createdAt, String screenName, String fullName, URL profileImageUrl,
            String text, List<TweetLink> links, List<TweetMention> mentions, List<TweetHashtag> hashtags,
            boolean retweet, String retweetUserName) {
        super(DocumentType.T, Tweet.urlOf(remoteTweetId, screenName), null, null);

        this.remoteTweetId = remoteTweetId;
        setCreationDate(createdAt);
        this.screenName = screenName;
        this.fullName = fullName;
        this.profileImageUrl = profileImageUrl.toExternalForm();

        this.setContent(text);

        this.links = links;
        this.mentions = mentions;
        this.hashtags = hashtags;
        this.retweet = retweet;
        this.retweetUserName = retweetUserName;
    }

    private static URL urlOf(Long remoteTweetId, String screenName) {
        try {
            return new URL("http://twitter.com/#!/" + screenName + "/status/" + remoteTweetId);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected Tweet() {
    }

    public Long getRemoteTweetId() {
        return this.remoteTweetId;
    }

    public Date getCreatedAt() {
        return super.getCreationDate();
    }

    public List<TweetHashtag> getHashtags() {
        return this.hashtags;
    }

    public List<TweetLink> getLinks() {
        return this.links;
    }

    public List<TweetMention> getMentions() {
        return this.mentions;
    }

    public String getScreenName() {
        return this.screenName;
    }

    public String getFullName() {
        return this.fullName;
    }

    public String getProfileImageUrl() {
        return this.profileImageUrl;
    }

    public boolean isRetweet() {
        return retweet;
    }

    public String getRetweetUserName() {
        return retweetUserName;
    }

    @Override
    public String getTitle() {
        if (this.title == null) {
            this.title = StringUtils.abbreviate(getContent(), 100);
        }

        return this.title;
    }
}
