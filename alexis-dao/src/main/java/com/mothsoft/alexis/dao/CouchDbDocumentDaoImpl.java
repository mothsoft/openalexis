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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.io.JsonStringEncoder;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ArrayNode;

import com.mothsoft.alexis.domain.DataRange;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentScore;
import com.mothsoft.alexis.domain.DocumentState;
import com.mothsoft.alexis.domain.Graph;
import com.mothsoft.alexis.domain.ImportantNamedEntity;
import com.mothsoft.alexis.domain.ImportantTerm;
import com.mothsoft.alexis.domain.SortOrder;
import com.mothsoft.alexis.domain.TopicDocument;
import com.mothsoft.alexis.util.HttpClientResponse;
import com.mothsoft.alexis.util.NetworkingUtil;

public class CouchDbDocumentDaoImpl implements DocumentDao {

    private static final String APPLICATION_JSON = "application/json";
    private static final String FIND_BY_URL_VIEW = "_design/views/_view/find_by_url?key=%%22%s%%22&include_docs=true";

    private static final String SEARCH_BY_USER = "?q=userId%%3Clong%%3E:%d&include_docs=true&skip=%d&limit=%d&sort=%%5CcreationDate";

    // ?q=+userId<long>:X +(X)&include_docs=true&skip=X&limit=X
    private static final String SEARCH_BY_USER_AND_EXPRESSION = "?q=%%2BuserId%%3Clong%%3E:%d%%20%%2B%%28%s%%29&include_docs=true&skip=%d&limit=%d";
    private static final String SORT_DATE_ASC = "&sort=creationDate";
    private static final String SORT_DATE_DESC = "&sort=%5CcreationDate";

    /* content constants */
    private static final String DOC = "doc";
    private static final String TOTAL_ROWS = "total_rows";
    private static final String ROWS = "rows";
    private static final String SCORE = "score";
    private static final String GMT = "GMT";

    private URL couchDbDatabaseUrl;
    private URL couchDbLuceneBaseUrl;
    private CredentialsProvider credentialsProvider;
    private ObjectMapper objectMapper;

    public CouchDbDocumentDaoImpl(final URL couchDbDatabaseUrl, final URL couchDbLuceneBaseUrl, final String username,
            final String password) {
        this.couchDbDatabaseUrl = couchDbDatabaseUrl;
        this.couchDbLuceneBaseUrl = couchDbLuceneBaseUrl;
        this.credentialsProvider = new BasicCredentialsProvider();
        this.credentialsProvider.setCredentials(
                new AuthScope(couchDbDatabaseUrl.getHost(), couchDbDatabaseUrl.getPort()),
                new UsernamePasswordCredentials(username, password));
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
    }

    @Override
    public void add(Document document) {
        final Date now = new Date();
        document.setCreationDate(now);

        final String content = this.toJSON(document);

        try {
            final HttpClientResponse response = NetworkingUtil.post(this.couchDbDatabaseUrl, content, APPLICATION_JSON,
                    this.credentialsProvider);
            final Map<String, Object> map = this.objectMapper.readValue(response.getInputStream(), Map.class);
            final String id = (String) map.get("id");
            final String rev = (String) map.get("rev");
            document.setId(id);
            document.setRev(rev);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Document findByUrl(String url) {

        final URL requestUrl;
        final HttpClientResponse response;
        try {
            requestUrl = new URL(this.couchDbDatabaseUrl.toExternalForm()
                    + this.toJSON(String.format(FIND_BY_URL_VIEW, url)));
            response = NetworkingUtil.get(requestUrl, null, null, this.credentialsProvider);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final JsonNode node;
        final Document document;
        try {
            node = this.objectMapper.readTree(response.getInputStream());
            final int totalRows = node.findValue(TOTAL_ROWS).getIntValue();
            final JsonNode docNode = node.findValue(DOC);

            if (totalRows == 0 || docNode == null) {
                document = null;
            } else {
                document = this.objectMapper.treeToValue(docNode, Document.class);
            }

        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return document;
    }

    @Override
    public Document get(String documentId) {
        URL documentUrl;
        try {
            documentUrl = new URL(this.couchDbDatabaseUrl + "/" + documentId);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        final HttpClientResponse response;
        try {
            response = NetworkingUtil.get(documentUrl, null, null, this.credentialsProvider);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            return this.objectMapper.readValue(response.getInputStream(), Document.class);
        } catch (JsonParseException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ImportantTerm> getImportantTerms(Long userId, Date startDate, Date endDate, int count,
            boolean filterStopWords) {
        return Collections.emptyList();
    }

    @Override
    public List<ImportantTerm> getImportantTerms(String documentId, int howMany, boolean filterStopWords) {
        return Collections.emptyList();
    }

    @Override
    public Graph getRelatedTerms(String query, Long userId, int howMany) {
        return new Graph();
    }

    @Override
    public List<TopicDocument> getTopicDocuments(String documentId) {
        return Collections.emptyList();
    }

    @Override
    public DataRange<Document> listDocumentsByOwner(Long userId, int start, int count) {
        int skip = Math.max(start - 1, 0);
        URL searchUrl;

        try {
            searchUrl = new URL(this.couchDbLuceneBaseUrl.toExternalForm()
                    + String.format(SEARCH_BY_USER, userId, skip, count));
            final HttpClientResponse response = NetworkingUtil.get(searchUrl, null, null, this.credentialsProvider);
            return buildSearchResultsRange(response, start, count);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DataRange<Document> buildSearchResultsRange(HttpClientResponse response, int start, int count) {
        final int totalRows;
        final List<Document> documents = new ArrayList<Document>(count);
        try {
            final JsonNode node = this.objectMapper.readTree(response.getInputStream());
            totalRows = node.findValue(TOTAL_ROWS).getIntValue();

            final JsonNode rowsNode = node.findValue(ROWS);
            final List<JsonNode> documentNodes = rowsNode.findValues(DOC);

            for (final JsonNode documentNode : documentNodes) {
                documents.add(this.objectMapper.convertValue(documentNode, Document.class));
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final DataRange<Document> range = new DataRange<Document>(documents, start, totalRows);
        return range;
    }

    private DataRange<DocumentScore> buildScoredSearchResultsRange(HttpClientResponse response, int start, int count) {
        final int totalRows;
        final List<DocumentScore> documentScores = new ArrayList<DocumentScore>(count);
        try {
            final JsonNode node = this.objectMapper.readTree(response.getInputStream());
            totalRows = node.findValue(TOTAL_ROWS).getIntValue();

            final ArrayNode rowsNode = (ArrayNode) node.findValue(ROWS);

            for (final Iterator<JsonNode> it = rowsNode.getElements(); it.hasNext();) {
                final JsonNode rowNode = it.next();
                final Document document = this.objectMapper.convertValue(rowNode.findValue(DOC), Document.class);

                final float score;
                final JsonNode scoreNode = rowNode.findValue(SCORE);

                if (scoreNode == null) {
                    score = 0.0f;
                } else {
                    score = (float) scoreNode.getDoubleValue();
                }
                documentScores.add(new DocumentScore(document, score));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final DataRange<DocumentScore> range = new DataRange<DocumentScore>(documentScores, start, totalRows);
        return range;
    }

    @Override
    public DataRange<Document> listDocumentsInTopicsByOwner(Long userId, int firstRecord, int numberOfRecords) {
        return new DataRange<Document>(Collections.EMPTY_LIST, 0, 0);
    }

    @Override
    public List<Document> listTopDocuments(Long userId, Date startDate, Date endDate, int count) {
        return Collections.emptyList();
    }

    @Override
    public DataRange<DocumentScore> searchByOwnerAndExpression(Long userId, String query, SortOrder sortOrder,
            int start, int count) {
        int skip = Math.max(start - 1, 0);
        String urlString;

        try {
            query = URLEncoder.encode(query, "UTF-8");
            urlString = this.couchDbLuceneBaseUrl.toExternalForm()
                    + String.format(SEARCH_BY_USER_AND_EXPRESSION, userId, query, skip, count);

            if (sortOrder == SortOrder.DATE_ASC) {
                urlString += SORT_DATE_ASC;
            } else if (sortOrder == SortOrder.DATE_DESC) {
                urlString += SORT_DATE_DESC;
            }

            final HttpClientResponse response = NetworkingUtil.get(new URL(urlString), null, null,
                    this.credentialsProvider);
            return buildScoredSearchResultsRange(response, start, count);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Document document) {
        final String content = this.toJSON(document);

        URL url;
        try {
            url = new URL(this.couchDbDatabaseUrl + "/" + document.getId());
        } catch (MalformedURLException e1) {
            throw new RuntimeException(e1);
        }
        try {
            final HttpClientResponse response = NetworkingUtil.put(url, content, APPLICATION_JSON,
                    this.credentialsProvider);
            final Map<String, Object> map = this.objectMapper.readValue(response.getInputStream(), Map.class);
            final String id = (String) map.get("id");
            final String rev = (String) map.get("rev");
            document.setId(id);
            document.setRev(rev);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ImportantNamedEntity> getImportantNamedEntities(Long userId, Date startDate, Date endDate, int howMany) {
        return Collections.emptyList();
    }

    @Override
    public List<ImportantNamedEntity> getImportantNamedEntitiesForDocument(String documentId, int howMany) {
        return Collections.emptyList();
    }

    public void remove(final Document document) {
        URL documentUrl;
        try {
            documentUrl = new URL(this.couchDbDatabaseUrl + "/" + document.getId() + "?rev=" + document.getRev());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        NetworkingUtil.delete(documentUrl, this.credentialsProvider);
    }

    private String toJSON(final Document document) {
        try {
            final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone(GMT));
            return this.objectMapper.writer(dateFormat).writeValueAsString(document);
        } catch (JsonGenerationException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String toJSON(final String text) {
        return new String(JsonStringEncoder.getInstance().encodeAsUTF8(text), Charset.forName("UTF-8"));
    }

    @Override
    public void addRawContent(String documentId, String rev, String content, String mimeType) {
        final URL documentUrl;
        try {
            documentUrl = new URL(this.couchDbDatabaseUrl + "/" + documentId + "/raw?rev=" + rev);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        try {
            NetworkingUtil.put(documentUrl, content, mimeType, this.credentialsProvider);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Document document = this.get(documentId);
        document.setState(DocumentState.FETCHED);
        document.setRetrievalDate(new Date());
        this.update(document);
    }

}
