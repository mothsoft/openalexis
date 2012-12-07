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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity(name = "RssFeed")
@Table(name = "rss_feed")
public class RssFeed {

    @Id
    @GeneratedValue
    private Long id;

    @Lob
    @Column(name = "url", columnDefinition = "text")
    private String url;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Lob
    @Column(name = "etag", columnDefinition = "text")
    private String etag;

    @Column(name = "retrieval_date")
    private Date retrievalDate;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "rss_feed_id")
    private List<RssSource> rssSources;

    public RssFeed(final String url) {
        this.url = url;
    }

    protected RssFeed() {

    }

    public Long getId() {
        return id;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(final String etag) {
        this.etag = etag;
    }

    public Date getRetrievalDate() {
        return this.retrievalDate;
    }

    public void setRetrievalDate(final Date retrievalDate) {
        this.retrievalDate = retrievalDate;
    }

    public List<RssSource> getRssSources() {
        return this.rssSources;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlDomain() throws MalformedURLException {
        final URL url = new URL(getUrl());
        return url.getHost();
    }
}
