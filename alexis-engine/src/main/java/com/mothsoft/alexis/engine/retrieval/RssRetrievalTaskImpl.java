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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.mothsoft.alexis.dao.DocumentDao;
import com.mothsoft.alexis.dao.RssFeedDao;
import com.mothsoft.alexis.dao.SourceDao;
import com.mothsoft.alexis.dao.UserDao;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentType;
import com.mothsoft.alexis.domain.DocumentUser;
import com.mothsoft.alexis.domain.RssFeed;
import com.mothsoft.alexis.domain.RssSource;
import com.mothsoft.alexis.domain.User;
import com.mothsoft.alexis.engine.textual.WebContentParser;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.util.HttpClientResponse;
import com.mothsoft.alexis.util.NetworkingUtil;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;

public class RssRetrievalTaskImpl implements RetrievalTask {

    private static final Logger logger = Logger.getLogger(RssRetrievalTaskImpl.class);

    private static final Comparator<SyndEntry> ENTRY_COMPARATOR;

    static {
        ENTRY_COMPARATOR = new Comparator<SyndEntry>() {
            public int compare(SyndEntry e1, SyndEntry e2) {
                // arbitrarily sort if no dates are available
                final Date date1 = mostRecentOfOrNow(e1.getPublishedDate(), e1.getUpdatedDate());
                final Date date2 = mostRecentOfOrNow(e2.getPublishedDate(), e2.getUpdatedDate());
                return date1.compareTo(date2);
            }

            private Date mostRecentOfOrNow(Date... dates) {
                Date newest = null;
                for (final Date date : dates) {
                    if (date == null) {
                        continue;
                    }
                    if (newest == null || date.after(newest)) {
                        newest = date;
                    }
                }

                if (newest == null) {
                    newest = new Date();
                }
                return newest;
            }
        };
    }

    private static final String DOCUMENT_ID = "DOCUMENT_ID";

    private DocumentDao documentDao;
    private RssFeedDao rssFeedDao;
    private SourceDao sourceDao;
    private UserDao userDao;
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;
    private WebContentParser webContentParser;

    // JMS queuing for document retrieval
    private JmsTemplate jmsTemplate;

    private IntelligentDelay delay;

    public RssRetrievalTaskImpl() {
        delay = new IntelligentDelay("RSS Retrieval", 5, 90);
    }

    public void setDocumentDao(final DocumentDao documentDao) {
        this.documentDao = documentDao;
    }

    public void setRssFeedDao(final RssFeedDao rssFeedDao) {
        this.rssFeedDao = rssFeedDao;
    }

    public void setSourceDao(final SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    public void setUserDao(final UserDao userDao) {
        this.userDao = userDao;
    }

    public void setTransactionManager(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.transactionTemplate = new TransactionTemplate(this.transactionManager);
    }

    public void setWebContentParser(final WebContentParser webContentParser) {
        this.webContentParser = webContentParser;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void retrieve() {

        CurrentUserUtil.setSystemUserAuthentication();

        try {
            final List<RssFeed> feeds = findFeedsToProcess();

            for (final RssFeed feed : feeds) {
                handleFeed(feed);
            }

            if (feeds.isEmpty()) {
                logger.info("RSS Retrieval found nothing to do, will sleep");
                this.delay.sleep();
            } else {
                this.delay.reset();
            }

        } finally {
            CurrentUserUtil.clearAuthentication();
        }
    }

    private List<RssFeed> findFeedsToProcess() {
        try {
            return this.transactionTemplate.execute(new TransactionCallback<List<RssFeed>>() {

                public List<RssFeed> doInTransaction(TransactionStatus status) {
                    return RssRetrievalTaskImpl.this.rssFeedDao.listRssFeedsWithRetrievalDateMoreThanXMinutesAgo(30);
                }
            });
        } catch (final Exception e) {
            logger.error("Listing sources for retrieval failed: " + e, e);
            return Collections.emptyList();
        }
    }

    private void handleFeed(final RssFeed feed) {
        try {
            long start = System.currentTimeMillis();
            this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {

                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    handleFeedImpl(feed);
                }
            });
            logger.info("RSS Feed: '" + feed.getUrl() + "' handled in " + (System.currentTimeMillis() - start)
                    + " milliseconds.");
        } catch (final Exception e) {
            logger.warn("RSS Feed: " + feed.getUrl() + " failed retrieval " + e, e);
        }
    }

    private void handleFeedImpl(final RssFeed rssFeed) {
        final long start = System.currentTimeMillis();

        final Date retrievalDate = new Date(start);
        rssFeed.setRetrievalDate(retrievalDate);

        final String url = rssFeed.getUrl();
        logger.info("Retrieving RSS feed: " + url);

        final SyndFeedInput input = new SyndFeedInput();
        URL feedUrl;
        HttpClientResponse response = null;
        InputStream is = null;

        try {
            feedUrl = new URL(url);
            response = NetworkingUtil.get(feedUrl, rssFeed.getEtag(), rssFeed.getLastModifiedDate());

            if (response.getStatusCode() == 304) {
                logger.info("No RSS feed changes -- skipping");
            } else {
                is = response.getInputStream();

                final SyndFeed feed = input.build(new com.sun.syndication.io.XmlReader(is));
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(response);
                
                rssFeed.setEtag(response.getEtag());
                rssFeed.setLastModifiedDate(response.getLastModifiedDate());

                logger.info("Parsing took: " + (System.currentTimeMillis() - start) + " milliseconds");

                @SuppressWarnings("unchecked")
                final List<SyndEntry> entries = feed.getEntries();

                // newer ones listed first--we want the older ones to be
                // processed first
                Collections.sort(entries, ENTRY_COMPARATOR);

                // FIXME - ugly. possible to replace DocumentUser with
                // SourceUser and DocumentSource? or just more complicated?
                for (final SyndEntry entry : entries) {
                    handleEntry(rssFeed, entry);
                }

                logger.info("RSS feed parsing complete.");
            }
        } catch (Exception e) {
            if (response != null) {
                response.abort();
            }
            // FIXME - consider trying again, tallying an error count, and
            // waiting 30 minutes after a certain number of errors
            logger.error("Error retrieving/parsing RSS feed at: " + url + ", will wait to try again.");
            logger.error("RSS Feed Error was: " + e, e);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(response);
        }

        this.rssFeedDao.update(rssFeed);

        // FIXME - consider optimizing this with a query
        for (final RssSource ithRssSource : rssFeed.getRssSources()) {
            ithRssSource.setRetrievalDate(retrievalDate);
            this.sourceDao.update(ithRssSource);
        }
    }

    private void handleEntry(final RssFeed rssFeed, final SyndEntry entry) {
        logger.info("Entry: " + entry.getLink());

        URL url = null;

        if (entry.getLink() != null) {

            try {
                url = new URL(entry.getLink());
            } catch (MalformedURLException e1) {
                logger.error("    Bad link: " + entry.getLink() + ", skipping");
                return;
            }

            Document document = this.documentDao.findByUrl(url.toExternalForm());

            if (document == null) {
                final String title = readTitle(entry);
                final String description = readDescription(entry);
                document = new Document(DocumentType.W, url, title, description);
                document.setCreationDate(this.firstNotNull(entry.getPublishedDate(), entry.getUpdatedDate(), new Date()));

                this.documentDao.add(document);
                this.queueForRetrieval(document);

            } else {
                logger.info("Document already exists, will not queue again.");
            }

            // FIXME - optimize, perhaps with an intelligent query
            for (final RssSource ithRssSource : rssFeed.getRssSources()) {
                final Long userId = ithRssSource.getUserId();
                final User user = this.userDao.get(userId);
                final DocumentUser documentUser = new DocumentUser(document.getId(), user.getId());

                if (!document.getDocumentUsers().contains(documentUser)) {
                    document.getDocumentUsers().add(documentUser);
                }
                this.documentDao.update(document);
            }

        }
    }

    private Date firstNotNull(Date... dates) {
        for (final Date date : dates) {
            if (date != null) {
                return date;
            }
        }
        return null;
    }

    private String readDescription(final SyndEntry syndEntry) {
        final SyndContent syndContent = syndEntry.getDescription();
        return readString(syndContent == null ? null : syndContent.getValue());
    }

    private String readString(String value) {
        if (value == null) {
            return null;
        }

        try {
            final byte[] bytes = value.getBytes("UTF-8");
            value = new String(bytes, "UTF-8");
            final String parsed = this.webContentParser.parseHTML(value);
            return parsed;
        } catch (IOException e) {
            logger.error("Failed to parse string '" + value + "' " + e, e);
            return null;
        }
    }

    private String readTitle(final SyndEntry syndEntry) {
        return readString(syndEntry == null ? null : syndEntry.getTitle());
    }

    private void queueForRetrieval(final Document document) {
        this.jmsTemplate.send(new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                final TextMessage message = session.createTextMessage();
                message.setStringProperty(DOCUMENT_ID, document.getId());
                logger.info("Sending document retrieval request for: " + document.getUrl());
                return message;
            }
        });
    }

}
