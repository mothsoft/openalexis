package com.mothsoft.alexis.engine.textual;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.mothsoft.alexis.dao.DocumentDao;
import com.mothsoft.alexis.dao.TopicDao;
import com.mothsoft.alexis.domain.DataRange;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentScore;
import com.mothsoft.alexis.domain.DocumentType;
import com.mothsoft.alexis.domain.SortOrder;
import com.mothsoft.alexis.domain.Topic;
import com.mothsoft.alexis.domain.TopicRef;
import com.mothsoft.alexis.engine.numeric.TopicActivityDataSetImporterImpl;

public class TopicDocumentMatcherTest {

    private TopicDocumentMatcher matcher;
    private TopicDao topicDao;
    private DocumentDao documentDao;
    private TopicActivityDataSetImporterImpl topicActivityDataSetImporterImpl;

    @Before
    public void setUp() {
        topicDao = mock(TopicDao.class);
        documentDao = mock(DocumentDao.class);
        this.topicActivityDataSetImporterImpl = mock(TopicActivityDataSetImporterImpl.class);
        this.matcher = new TopicDocumentMatcherImpl(topicDao, documentDao, topicActivityDataSetImporterImpl);
    }

    @Test
    public void testMatch() throws MalformedURLException {
        final DocumentType type = DocumentType.W;
        final URL url = new URL("http://server");
        final String title = "Hello";
        final String description = "World";
        final Document document = new Document(type, url, title, description);
        document.setId(UUID.randomUUID().toString());

        // happy path
        final List<Topic> topics = new ArrayList<Topic>();
        final Topic topic1 = new Topic();
        topic1.setSearchExpression("lorem ipsum dolor");
        topic1.setUserId(1L);
        topics.add(topic1);
        when(this.topicDao.listTopicsByOwner(1L)).thenReturn(topics);

        final List<DocumentScore> range = new ArrayList<DocumentScore>(1);
        range.add(new DocumentScore(document, 2.3f));
        final DataRange<DocumentScore> documentScores = new DataRange<DocumentScore>(range, 1, 1);
        when(
                this.documentDao.searchByOwnerAndExpression(1L, "+id:" + document.getId() + " lorem ipsum dolor",
                        SortOrder.RELEVANCE, 1, 1)).thenReturn(documentScores);

        final List<TopicRef> topicRefs = this.matcher.match(document, 1L);
        assertNotNull(topicRefs);
        assertEquals(1, topicRefs.size());
    }
}
