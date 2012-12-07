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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "TweetHashtag")
@Table(name = "tweet_hashtag")
public class TweetHashtag {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tweet_id")
    private Tweet tweet;

    @Column(name = "start", columnDefinition = "smallint")
    private short start;

    @Column(name = "end", columnDefinition = "smallint")
    private short end;

    @Column(name = "hashtag", length = 140)
    private String hashtag;

    public TweetHashtag(short start, short end, String hashtag) {
        this.start = start;
        this.end = end;
        this.hashtag = hashtag;
    }

    protected TweetHashtag() {
    }

    public Long getId() {
        return id;
    }

    public Tweet getTweet() {
        return tweet;
    }

    public short getStart() {
        return start;
    }

    public short getEnd() {
        return end;
    }

    public String getHashtag() {
        return hashtag;
    }

    void setTweet(Tweet tweet) {
        this.tweet = tweet;
    }

}
