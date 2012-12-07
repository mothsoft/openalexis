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
package com.mothsoft.integration.twitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.PropertyConfiguration;
import twitter4j.internal.logging.Logger;

public class TwitterServiceImpl implements TwitterService {

    private static final Logger logger = Logger.getLogger(TwitterServiceImpl.class);

    private Configuration configuration;
    private TwitterFactory factory;

    public TwitterServiceImpl(final Properties properties) {
        this.configuration = new PropertyConfiguration(properties);
        this.factory = new TwitterFactory(this.configuration);
    }

    public User login(final String oauthToken, final String oauthTokenSecret) {
        try {
            final Twitter twitter = this.factory.getInstance(new AccessToken(oauthToken, oauthTokenSecret));
            User user = twitter.verifyCredentials();
            return user;
        } catch (TwitterException e) {
            throw wrapException(e);
        }
    }

    public void createSavedSearch(final String query) {
        try {
            final Twitter twitter = factory.getInstance();
            twitter.createSavedSearch(query);
        } catch (TwitterException e) {
            throw wrapException(e);
        }
    }

    public List<SavedSearch> listSavedSearches() {
        try {
            final Twitter twitter = factory.getInstance();
            final List<SavedSearch> savedSearches = twitter.getSavedSearches();
            return savedSearches;
        } catch (TwitterException e) {
            throw wrapException(e);
        }
    }

    public List<Tweet> search(final String query) {
        try {
            final Twitter twitter = factory.getInstance();
            final QueryResult queryResult = twitter.search(new Query(query));
            final List<Tweet> tweets = queryResult.getTweets();
            return tweets;
        } catch (TwitterException e) {
            throw wrapException(e);
        }
    }

    private TwitterServiceException wrapException(Exception e) {
        return new TwitterServiceException(e);
    }

    public RequestToken getRequestToken() {
        final Twitter twitter = factory.getInstance();
        try {
            return twitter.getOAuthRequestToken();
        } catch (TwitterException e) {
            throw this.wrapException(e);
        }
    }

    public AccessToken getAccessToken(final RequestToken requestToken, final String verificationCode) {
        final Twitter twitter = factory.getInstance();
        try {
            return twitter.getOAuthAccessToken(requestToken, verificationCode);
        } catch (TwitterException e) {
            throw this.wrapException(e);
        }
    }

    @SuppressWarnings("deprecation")
    public List<Status> getHomeTimeline(AccessToken accessToken, Long sinceId, Short maximumNumber) {
        final Twitter twitter = this.factory.getInstance(accessToken);
        final List<Status> statuses = new ArrayList<Status>(maximumNumber);

        // default maximum number to 200 if null
        maximumNumber = maximumNumber == null ? 200 : maximumNumber;

        // default page size to lesser of maximumNumber, 200
        final int pageSize = maximumNumber > 200 ? 200 : maximumNumber;
        int page = 0;

        while (statuses.size() < maximumNumber) {
            Paging paging = new Paging(++page, pageSize);
            final ResponseList temp;

            if (sinceId != null) {
                paging = paging.sinceId(sinceId);
            }

            try {
                temp = twitter.getHomeTimeline(paging);
            } catch (TwitterException e) {
                throw this.wrapException(e);
            }

            // break out as soon as we get a page smaller than the designated
            // page size
            if (temp.size() == 0) {
                break;
            } else {
                statuses.addAll(temp);
            }

            // check rate limit status and warn or skip remaining fetches as
            // appropriate
            final RateLimitStatus rateLimitStatus = temp.getRateLimitStatus();
            if (rateLimitStatus.getRemainingHits() < (.1 * rateLimitStatus.getHourlyLimit())) {
                logger.warn("Twitter rate limit approaching. Calls remaining: " + rateLimitStatus.getRemainingHits());
            }

            if (rateLimitStatus.getRemainingHits() == 0) {
                logger.error("Twitter rate limit hit.  Will reset at: "
                        + rateLimitStatus.getResetTime().toLocaleString());
                break;
            }
        }

        return statuses;
    }
}
