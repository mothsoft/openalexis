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
package com.mothsoft.alexis.service.impl;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.dao.RssFeedDao;
import com.mothsoft.alexis.dao.SourceDao;
import com.mothsoft.alexis.domain.RssFeed;
import com.mothsoft.alexis.domain.RssSource;
import com.mothsoft.alexis.domain.Source;
import com.mothsoft.alexis.domain.TwitterSource;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.SourceService;

@Transactional
public class SourceServiceImpl implements SourceService {

    private SourceDao sourceDao;
    private RssFeedDao rssFeedDao;

    public void setSourceDao(final SourceDao sourceDao) {
        this.sourceDao = sourceDao;
    }

    public void setRssFeedDao(final RssFeedDao rssFeedDao) {
        this.rssFeedDao = rssFeedDao;
    }

    @Override
    public void add(RssSource source) {
        this.sourceDao.add(source);
    }

    @Override
    public void add(TwitterSource source) {
        this.sourceDao.add(source);
    }

    public RssFeed findOrCreateRssFeed(final String url) {
        RssFeed feed = this.rssFeedDao.findByUrl(url);

        if (feed == null) {
            feed = new RssFeed(url);
            this.rssFeedDao.add(feed);
        }

        return feed;
    }

    @Override
    public List<Source> listAllSourcesByOwner(Long userId) {
        return this.sourceDao.listSourcesByOwner(userId, Source.class);
    }

    @Override
    public List<Source> listRssSourcesByOwner(Long userId) {
        return this.sourceDao.listSourcesByOwner(userId, RssSource.class);
    }

    @Override
    public List<Source> listTwitterSourcesByOwner(Long userId) {
        return this.sourceDao.listSourcesByOwner(userId, TwitterSource.class);
    }

    public void remove(final Long id) {
        final Source source = this.sourceDao.get(id);

        if (!source.getUserId().equals(CurrentUserUtil.getCurrentUserId())) {
            throw new SecurityException("Access Denied");
        }

        RssFeed feed = null;
        if (source instanceof RssSource) {
            feed = ((RssSource) source).getFeed();
        }

        this.sourceDao.remove(source);

        if (feed != null) {
            removeRssFeedIfUnused(feed);
        }
    }

    private void removeRssFeedIfUnused(final RssFeed feed) {
        if (feed.getRssSources().isEmpty()) {
            this.rssFeedDao.remove(feed);
        }
    }

}
