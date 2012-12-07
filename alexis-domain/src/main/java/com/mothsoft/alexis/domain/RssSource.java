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

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PreRemove;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.apache.log4j.Logger;

@Entity(name = "RssSource")
@Table(name = "rss_source")
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
public class RssSource extends Source {

    private static final Logger logger = Logger.getLogger(RssSource.class);

    @ManyToOne(optional = false)
    @JoinColumn(name = "rss_feed_id")
    private RssFeed feed;

    public RssSource(final RssFeed feed, final Long userId) {
        super(userId);
        this.feed = feed;
    }

    protected RssSource() {

    }

    @PreRemove
    protected void preRemove() {
        feed.getRssSources().remove(this);
        feed = null;
    }

    public String getDescription() {
        return getUrl();
    }

    public RssFeed getFeed() {
        return this.feed;
    }

    public String getUrl() {
        return this.feed.getUrl();
    }

    public String getUrlDomain() {
        try {
            return this.feed.getUrlDomain();
        } catch (MalformedURLException e) {
            logger.warn("Malformed feed URL : " + this.feed.getUrl());
            return "";
        }
    }

    @Override
    public SourceType getSourceType() {
        return SourceType.R;
    }

}
