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
package com.mothsoft.alexis.engine.retrieval;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import twitter4j.auth.AccessToken;

import com.mothsoft.alexis.dao.DocumentDao;
import com.mothsoft.alexis.dao.SourceDao;
import com.mothsoft.alexis.dao.TweetDao;
import com.mothsoft.alexis.dao.UserDao;
import com.mothsoft.alexis.domain.DocumentUser;
import com.mothsoft.alexis.domain.SocialConnection;
import com.mothsoft.alexis.domain.Source;
import com.mothsoft.alexis.domain.Tweet;
import com.mothsoft.alexis.domain.TweetHashtag;
import com.mothsoft.alexis.domain.TweetLink;
import com.mothsoft.alexis.domain.TweetMention;
import com.mothsoft.alexis.domain.TwitterSource;
import com.mothsoft.alexis.domain.User;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.integration.twitter.TwitterService;

public class TwitterRetrievalTaskImpl implements RetrievalTask {

    private static final Logger logger = Logger.getLogger(TwitterRetrievalTaskImpl.class);

    private DocumentDao documentDao;
    private SourceDao sourceDao;
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;
    private TweetDao tweetDao;
    private TwitterService twitterService;
    private UserDao userDao;

    private IntelligentDelay delay;

    public TwitterRetrievalTaskImpl() {
        delay = new IntelligentDelay("Twitter Retrieval", 5, 90);
    }

    public void setDocumentDao(final DocumentDao documentDao) {
        this.documentDao = documentDao;
    }

    public void setSourceDao(final SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    public void setTransactionManager(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.transactionTemplate = new TransactionTemplate(this.transactionManager);
    }

    public void setTweetDao(final TweetDao tweetDao) {
        this.tweetDao = tweetDao;
    }

    public void setTwitterService(final TwitterService twitterService) {
        this.twitterService = twitterService;
    }

    public void setUserDao(final UserDao userDao) {
        this.userDao = userDao;
    }

    public void retrieve() {

        CurrentUserUtil.setSystemUserAuthentication();

        try {
            logger.info("Retrieving content from Twitter");

            final List<Source> sources = findSourcesToProcess();

            for (final Source source : sources) {
                final TwitterSource twitterSource = (TwitterSource) source;

                logger.info("Retrieving Twitter source: " + twitterSource.getId() + " using Twitter user: "
                        + twitterSource.getSocialConnection().getRemoteUsername());
                handleSource(twitterSource);

            }

            if (CollectionUtils.isEmpty(sources)) {
                logger.info("Twitter retrieval found nothing to do, will sleep");
                this.delay.sleep();
            }

        } catch (final Exception e) {
            logger.error("Exception will cause falloff: " + e, e);
            this.delay.sleep();
        } finally {
            CurrentUserUtil.clearAuthentication();
        }
    }

    private List<Source> findSourcesToProcess() {
        try {
            return this.transactionTemplate.execute(new TransactionCallback<List<Source>>() {

                public List<Source> doInTransaction(TransactionStatus status) {
                    return TwitterRetrievalTaskImpl.this.sourceDao.listSourcesWithRetrievalDateMoreThanXMinutesAgo(15,
                            TwitterSource.class);

                }
            });
        } catch (final Exception e) {
            logger.error("Listing sources for retrieval failed: " + e, e);
            return Collections.emptyList();
        }
    }

    private void handleSource(final TwitterSource twitterSource) {
        try {
            this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {

                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    handleSourceImpl(twitterSource);
                }
            });
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleSourceImpl(final TwitterSource twitterSource) {
        final SocialConnection socialConnection = twitterSource.getSocialConnection();
        final AccessToken accessToken = new AccessToken(socialConnection.getOauthToken(),
                socialConnection.getOauthTokenSecret());

        final List<Status> statuses = this.twitterService.getHomeTimeline(accessToken, twitterSource.getLastTweetId(),
                (short) 800);

        if (statuses != null && statuses.size() > 0) {
            logger.info("Twitter retrieval found " + statuses.size() + " Tweets for user: "
                    + socialConnection.getRemoteUsername());

            // the newest tweet in the timeline will be our starting point for
            // the next fetch
            twitterSource.setLastTweetId(statuses.get(0).getId());

            // import these in reverse order to ensure newest ones have the
            // highest document IDs
            Collections.reverse(statuses);

            final Long userId = twitterSource.getUserId();
            final User user = this.userDao.get(userId);

            for (final Status status : statuses) {
                final Long tweetId = status.getId();

                Tweet tweet = this.tweetDao.getTweetByRemoteTweetId(tweetId);
                final boolean isAdd = (tweet == null);

                if (isAdd) {
                    // TODO - is this right?
                    // Twitter allows 2 different styles of retweets. The
                    // ones that are actually retweets show as tweeted by the
                    // original user. Others may show
                    // "RT @original thing original said" tweeted
                    // by the new person
                    final boolean retweet = status.isRetweet();

                    final twitter4j.User tweeter;
                    final String text;
                    twitter4j.User retweeter = null;

                    final List<TweetLink> links;
                    final List<TweetMention> mentions;
                    final List<TweetHashtag> hashtags;

                    if (retweet) {
                        tweeter = status.getRetweetedStatus().getUser();
                        text = status.getRetweetedStatus().getText();
                        retweeter = status.getUser();
                        links = readLinks(status.getRetweetedStatus());
                        mentions = readMentions(status.getRetweetedStatus());
                        hashtags = readHashtags(status.getRetweetedStatus());
                    } else {
                        tweeter = status.getUser();
                        text = status.getText();
                        links = readLinks(status);
                        mentions = readMentions(status);
                        hashtags = readHashtags(status);
                    }

                    final URL profileImageUrl = tweeter.getProfileImageUrlHttps();
                    final Date createdAt = status.getCreatedAt();

                    tweet = new Tweet(tweetId, createdAt, tweeter.getScreenName(), tweeter.getName(), profileImageUrl,
                            text, links, mentions, hashtags, retweet, retweet ? retweeter.getScreenName() : null);
                    this.documentDao.add(tweet);
                }

                final DocumentUser documentUser = new DocumentUser(tweet, user);

                if (isAdd || !tweet.getDocumentUsers().contains(documentUser)) {
                    tweet.getDocumentUsers().add(new DocumentUser(tweet, user));
                    this.documentDao.update(tweet);
                }
            }
        } else {
            logger.info("Twitter retrieval found no Tweets for user: " + socialConnection.getRemoteUsername());
        }

        twitterSource.setRetrievalDate(new Date());
        this.sourceDao.update(twitterSource);
    }

    private List<TweetLink> readLinks(Status status) {
        final List<TweetLink> links = new ArrayList<TweetLink>();

        if (status.getURLEntities() != null) {
            for (final URLEntity entity : status.getURLEntities()) {
                final String displayUrl = entity.getDisplayURL();
                final String expandedUrl = entity.getExpandedURL() == null ? null : entity.getExpandedURL()
                        .toExternalForm();
                final String url = entity.getURL() == null ? null : entity.getURL().toExternalForm();
                final TweetLink link = new TweetLink((short) entity.getStart(), (short) entity.getEnd(), displayUrl,
                        expandedUrl, url);
                links.add(link);
            }
        }

        return links;
    }

    private List<TweetMention> readMentions(Status status) {
        final List<TweetMention> mentions = new ArrayList<TweetMention>();

        if (status.getUserMentionEntities() != null) {
            for (final UserMentionEntity entity : status.getUserMentionEntities()) {

                final Long userId = entity.getId();
                final String name = entity.getName();
                final String screenName = entity.getScreenName();

                final TweetMention mention = new TweetMention((short) entity.getStart(), (short) entity.getEnd(),
                        userId, name, screenName);
                mentions.add(mention);
            }
        }

        return mentions;
    }

    private List<TweetHashtag> readHashtags(Status status) {
        final List<TweetHashtag> hashtags = new ArrayList<TweetHashtag>();

        if (status.getHashtagEntities() != null) {
            for (final HashtagEntity entity : status.getHashtagEntities()) {

                final TweetHashtag hashtag = new TweetHashtag((short) entity.getStart(), (short) entity.getEnd(),
                        entity.getText());
                hashtags.add(hashtag);
            }
        }

        return hashtags;
    }
}
