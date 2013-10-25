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
package com.mothsoft.alexis.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.mothsoft.alexis.domain.DataRange;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentScore;
import com.mothsoft.alexis.domain.DocumentType;
import com.mothsoft.alexis.domain.DocumentUser;
import com.mothsoft.alexis.domain.SortOrder;
import com.mothsoft.alexis.domain.Tweet;
import com.mothsoft.alexis.domain.TweetHashtag;
import com.mothsoft.alexis.domain.TweetLink;
import com.mothsoft.alexis.domain.TweetMention;

public class CouchDbDocumentDaoImplTest {

    private CouchDbDocumentDaoImpl dao;

    private List<Document> documents;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws MalformedURLException {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);

        this.documents = new ArrayList<Document>(128);

        final URL dbUrl = new URL("http://localhost:5984/alexis/");
        final URL luceneUrl = new URL("http://localhost:5984/_fti/local/alexis/_design/search/all");
        this.dao = new CouchDbDocumentDaoImpl(dbUrl, luceneUrl, "admin", "admin");
    }

    @After
    public void tearDown() {
        for (final Document document : this.documents) {
            if (document.getId() != null) {
                final Document documentLatestRev = this.dao.get(document.getId());
                this.dao.remove(documentLatestRev);
            }
        }
    }

    @Test
    public void testAddDocument() throws MalformedURLException {
        final Document document = new Document(DocumentType.W, new URL("http://www.mothsoft.com/contact/"), "Contact",
                "Lorem ipsum dolor");
        document.setContent("Lorem ipsum dolor");
        this.documents.add(document);
        this.dao.add(document);
        assertNotNull(document.getId());
        assertNotNull(document.getRev());

        // test for round trip persistence
        final Document fetched = this.dao.get(document.getId());
        assertEquals(document.getId(), fetched.getId());
        assertEquals(document.getRev(), fetched.getRev());
    }

    @Test
    public void testAddDocumentDifficultJson() throws IOException {
        final InputStream is = this.getClass().getClassLoader().getResourceAsStream("difficult.json");

        final Tweet tweet = this.objectMapper.readValue(is, Tweet.class);
        this.documents.add(tweet);
        this.dao.add(tweet);
    }

    @Test
    public void testUpdateDocument() throws MalformedURLException {
        final Document document = new Document(DocumentType.W, new URL("http://www.mothsoft.com/"), "Mothsoft",
                "Lorem ipsum dolor");
        document.setContent("Lorem ipsum dolor");
        this.documents.add(document);
        this.dao.add(document);

        final String id = document.getId();
        final String firstRev = document.getRev();

        assertNotNull(id);
        assertNotNull(firstRev);

        this.dao.update(document);

        assertEquals(id, document.getId());
        assertNotSame(firstRev, document.getRev());
    }

    @Test
    public void testFindByUrl() throws MalformedURLException {
        final String url = "http://foo/" + Math.random();
        final Document document = new Document(DocumentType.W, new URL(url), "abc", "abc123");
        document.setContent("def456");
        this.documents.add(document);
        this.dao.add(document);

        final String id = document.getId();
        final String firstRev = document.getRev();

        assertNotNull(id);
        assertNotNull(firstRev);

        final Document fetched = this.dao.findByUrl(url);
        assertNotNull(fetched);
        assertEquals(id, fetched.getId());
    }

    @Test
    public void testNegativeFindByUrl() {
        assertNull(this.dao.findByUrl("http://abc.xyz.123.com/"));
    }

    @Test
    public void testAddRawContent() throws MalformedURLException {
        final String url = "http://foo/" + Math.random();
        final Document document = new Document(DocumentType.W, new URL(url), "abc", "abc123");
        this.documents.add(document);
        this.dao.add(document);

        this.dao.addRawContent(document.getId(), document.getRev(), "Lots of raw content here", "text/plain");

        assertEquals("Lots of raw content here", this.dao.getRawContent(document.getId()));
    }

    @Test
    public void testAddContent() throws MalformedURLException {
        final String url = "http://foo/" + Math.random();
        final Document document = new Document(DocumentType.W, new URL(url), "abc", "abc123");
        this.documents.add(document);
        this.dao.add(document);

        this.dao.addContent(document.getId(), document.getRev(), "Extracted content", "text/plain");
        assertEquals("Extracted content", this.dao.getContent(document.getId()));
    }

    @Test
    public void testListDocumentsByOwner() throws MalformedURLException {
        final String url = "http://foo/" + Math.random();
        final Long userId = 12345L;

        final Document document = new Document(DocumentType.W, new URL(url), "abc", "abc123");
        document.getDocumentUsers().add(new DocumentUser(null, userId));
        this.documents.add(document);
        this.dao.add(document);

        assertNotNull(document);
        assertNotNull(document.getId());

        final Document document2 = new Document(DocumentType.W, new URL(url), "abc", "abc123");
        document2.getDocumentUsers().add(new DocumentUser(null, userId));
        this.documents.add(document2);
        this.dao.add(document2);

        assertNotNull(document2);
        assertNotNull(document2.getId());

        final DataRange<Document> documentsByOwner = this.dao.listDocumentsByOwner(userId, 1, 9999);
        assertNotNull(documentsByOwner);
        assertTrue(documentsByOwner.getRange().size() >= 2);

        boolean found = false;
        for (final Document toExamine : documentsByOwner.getRange()) {
            if (document.getId().equals(toExamine.getId())) {
                found = true;
                break;
            }
        }
        assertTrue("expected to find document", found);
    }

    @Test
    public void testSearchByOwnerAndExpression() throws MalformedURLException {
        final String url = "http://foo/" + Math.random();
        final Long userId = 12345L;

        final Document document = new Document(DocumentType.W, new URL(url), "abc", "hufflepuffery");
        document.getDocumentUsers().add(new DocumentUser(null, userId));
        this.documents.add(document);
        this.dao.add(document);

        assertNotNull(document);
        assertNotNull(document.getId());

        final String query = "hufflepuffery";
        final DataRange<DocumentScore> range = this.dao.searchByOwnerAndExpression(12345L, query, SortOrder.RELEVANCE,
                1, 10);
        assertEquals(1, range.getRange().size());
        assertEquals(document.getId(), range.getRange().get(0).getDocument().getId());

    }

    @Test
    public void testFindTweetByTweetId() throws MalformedURLException {
        final List<TweetLink> links = new ArrayList<TweetLink>();
        links.add(new TweetLink((short) 3, (short) 6, "http://www.google.com", "http://www.google.com",
                "http://www.google.com"));

        final List<TweetMention> mentions = new ArrayList<TweetMention>();
        mentions.add(new TweetMention((short) 9, (short) 12, 12345L, "abc", "def"));

        final List<TweetHashtag> hashtags = new ArrayList<TweetHashtag>();
        hashtags.add(new TweetHashtag((short) 14, (short) 18, "#fools"));

        @SuppressWarnings("unchecked")
        final Tweet tweet = new Tweet(12345L, new Date(), "screen1", "abc 123", new URL("http://foo"), "text", links,
                mentions, hashtags, true, "def789");
        this.documents.add(tweet);
        this.dao.add(tweet);

        assertNotNull(this.dao.findTweetByTweetId(12345L));
    }
}
