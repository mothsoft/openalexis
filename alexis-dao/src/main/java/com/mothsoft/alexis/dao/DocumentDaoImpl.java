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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.util.Version;
import org.hibernate.CacheMode;
import org.hibernate.QueryException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.indexes.IndexReaderAccessor;
import org.springframework.stereotype.Repository;

import com.mothsoft.alexis.domain.DataRange;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.DocumentContent;
import com.mothsoft.alexis.domain.DocumentScore;
import com.mothsoft.alexis.domain.DocumentState;
import com.mothsoft.alexis.domain.DocumentTerm;
import com.mothsoft.alexis.domain.Edge;
import com.mothsoft.alexis.domain.Graph;
import com.mothsoft.alexis.domain.ImportantNamedEntity;
import com.mothsoft.alexis.domain.ImportantTerm;
import com.mothsoft.alexis.domain.Node;
import com.mothsoft.alexis.domain.SortOrder;
import com.mothsoft.alexis.domain.StopWords;
import com.mothsoft.alexis.domain.TFIDF;
import com.mothsoft.alexis.domain.TopicDocument;
import com.mothsoft.alexis.security.CurrentUserUtil;

@Repository
public class DocumentDaoImpl implements DocumentDao {

    private static final Logger logger = Logger.getLogger(DocumentDaoImpl.class);
    private static final DocumentState ANY_DOCUMENT_STATE = null;
    private static final Date NO_DATE = null;
    private static final String CONTENT_TEXT_FIELD_NAME = "content.text";

    @PersistenceContext
    private EntityManager em;

    public DocumentDaoImpl() throws IOException {
    }

    public void setEm(final EntityManager em) {
        this.em = em;
    }

    public void add(final Document document) {
        this.em.persist(document);
    }

    public void add(final DocumentContent content) {
        this.em.persist(content);
    }

    public void bulkUpdateDocumentState(DocumentState queryState, DocumentState nextState) {

        final Query query = this.em
                .createQuery("SELECT d FROM Document d WHERE d.intState = :queryState ORDER BY d.id ASC");
        query.setParameter("queryState", queryState.getValue());

        @SuppressWarnings("unchecked")
        final List<Document> documents = query.getResultList();

        for (final Document document : documents) {
            document.setState(nextState);
        }
    }

    public Document findByUrl(final String url) {
        final Query query = this.em.createQuery("FROM Document WHERE url = :url");
        query.setParameter("url", url);

        @SuppressWarnings("unchecked")
        final List<Document> results = query.getResultList();

        if (results.size() == 1) {
            return results.get(0);
        }

        return null;
    }

    public Document get(final Long id) {
        if (CurrentUserUtil.isSystem()) {
            return this.em.find(Document.class, id);
        } else {
            final Long userId = CurrentUserUtil.getCurrentUserId();

            final Query query = this.em
                    .createQuery("select d from Document d inner join d.documentUsers du inner join du.user user "
                            + "where user.id = :userId and d.id = :docId");
            query.setParameter("userId", userId);
            query.setParameter("docId", id);

            final Document document = (Document) query.getSingleResult();
            return document;
        }
    }

    public void update(final Document document) {
        this.em.merge(document);
    }

    public DataRange<Document> listDocumentsByOwner(final Long userId, final int first, final int count) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final SortOrder sortOrder = SortOrder.DATE_DESC;
        final DataRange<DocumentScore> scoredRange = this.searchWithAllOptions(userId, false, null, null, sortOrder,
                null /* ignore start date */, null /* ignore end date */, first, count);

        final List<Document> range = new ArrayList<Document>(scoredRange.getRange().size());

        for (final DocumentScore scoredDoc : scoredRange.getRange()) {
            range.add(scoredDoc.getDocument());
        }

        final DataRange<Document> dataRange = new DataRange<Document>(range, scoredRange.getFirstRow(),
                scoredRange.getTotalRowsAvailable());

        stopWatch.stop();
        logger.debug(stopWatch.toString());

        return dataRange;
    }

    public DataRange<Document> listDocumentsInTopicsByOwner(final Long userId, final int first, final int count) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final SortOrder sortOrder = SortOrder.DATE_DESC;
        final DataRange<DocumentScore> scoredRange = this.searchWithAllOptions(userId, true,
                DocumentState.MATCHED_TO_TOPICS, null, sortOrder, null, null, first, count);

        final List<Document> range = new ArrayList<Document>(scoredRange.getRange().size());

        for (final DocumentScore scoredDoc : scoredRange.getRange()) {
            range.add(scoredDoc.getDocument());
        }

        final DataRange<Document> dataRange = new DataRange<Document>(range, scoredRange.getFirstRow(),
                scoredRange.getTotalRowsAvailable());

        stopWatch.stop();
        logger.debug(stopWatch.toString());

        return dataRange;
    }

    public Document findAndLockOneDocument(final DocumentState state) {
        final Query query = this.em.createQuery("from Document where intState = :state order by id asc");
        query.setParameter("state", state.getValue());
        query.setMaxResults(1);

        @SuppressWarnings("unchecked")
        final List<Document> results = query.getResultList();

        if (results.isEmpty()) {
            return null;
        }

        final Document document = results.get(0);
        document.lock();
        this.em.merge(document);
        return document;
    }

    public List<ImportantTerm> getImportantTerms(Long userId, Date startDate, Date endDate, int count,
            boolean filterStopWords) {
        final FullTextQuery fullTextQuery = this.buildFullTextQuery(null, userId, startDate, endDate, false,
                ANY_DOCUMENT_STATE, FullTextQuery.DOCUMENT_ID);
        return getImportantTerms(fullTextQuery, count, filterStopWords);
    }

    @SuppressWarnings("unchecked")
    private List<ImportantTerm> getImportantTerms(FullTextQuery fullTextQuery, int count, boolean filterStopWords) {
        final Long start = System.currentTimeMillis();
        final List<Object[]> results = fullTextQuery.list();
        final LinkedHashMap<String, Tuple<Integer, Float>> termCountMap = new LinkedHashMap<String, Tuple<Integer, Float>>();

        final FullTextSession fullTextSession = Search.getFullTextSession((Session) this.em.getDelegate());
        final SearchFactory searchFactory = fullTextSession.getSearchFactory();
        final IndexReaderAccessor ira = searchFactory.getIndexReaderAccessor();
        final IndexReader reader = ira.open(com.mothsoft.alexis.domain.Document.class);
        final IndexSearcher searcher = new IndexSearcher(reader);

        final List<ImportantTerm> importantTerms;
        final int numDocs;
        try {
            numDocs = reader.numDocs();
            Term luceneTerm = new Term(CONTENT_TEXT_FIELD_NAME);

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("Found %d matching Lucene documents of %d in reader", results.size(),
                        numDocs));
            }

            // loop over all the matching documents
            for (final Object[] ith : results) {
                int docId = ((Number) ith[0]).intValue();
                final TermFreqVector tfv = reader.getTermFreqVector(docId, CONTENT_TEXT_FIELD_NAME);

                if (tfv == null) {
                    continue;
                }

                final String[] terms = tfv.getTerms();
                final int[] freqs = tfv.getTermFrequencies();

                // total document size
                int size = 0;

                for (int freq : freqs) {
                    size += freq;
                }

                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Lucene document %d has %d terms, to be merged with running count %d",
                            docId, size, termCountMap.size()));
                }

                // loop over the terms and aggregate the counts and tf-idf
                int i = 0;
                for (final String term : terms) {
                    if (StopWords.ENGLISH.contains(term)) {
                        continue;
                    }

                    luceneTerm = luceneTerm.createTerm(term);
                    final int termCount = freqs[i++];

                    final Tuple<Integer, Float> countScore;
                    if (termCountMap.containsKey(term)) {
                        countScore = termCountMap.get(term);
                        countScore.t1 += termCount;
                        countScore.t2 += (TFIDF.score(term, termCount, size, numDocs, searcher.docFreq(luceneTerm)));
                    } else {
                        countScore = new Tuple<Integer, Float>();
                        countScore.t1 = termCount;
                        countScore.t2 = (TFIDF.score(term, termCount, size, numDocs, searcher.docFreq(luceneTerm)));
                        termCountMap.put(term, countScore);
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Completed Lucene document processing.");
            }

            importantTerms = new ArrayList<ImportantTerm>(termCountMap.size());

            // find max TF-IDF
            float maxTfIdf = 0.0f;
            for (final Tuple<Integer, Float> ith : termCountMap.values()) {
                if (ith.t2 > maxTfIdf) {
                    maxTfIdf = ith.t2;
                }
            }

            for (final Map.Entry<String, Tuple<Integer, Float>> entry : termCountMap.entrySet()) {
                final int ithCount = entry.getValue().t1;
                final float ithTfIdf = entry.getValue().t2;
                importantTerms.add(new ImportantTerm(entry.getKey(), ithCount, ithTfIdf, maxTfIdf));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Completed term aggregation, will clear term map");
            }

            termCountMap.clear();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                searcher.close();
            } catch (IOException e) {
                logger.warn("Failed to close searcher: " + e, e);
            }
            ira.close(reader);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Sorting terms");
        }

        Collections.sort(importantTerms, new Comparator<ImportantTerm>() {
            @Override
            public int compare(ImportantTerm term1, ImportantTerm term2) {
                return -1 * term1.getTfIdf().compareTo(term2.getTfIdf());
            }
        });

        if (logger.isDebugEnabled()) {
            logger.debug("Term sort complete");
        }

        if (importantTerms.isEmpty() || importantTerms.size() < count) {
            if (logger.isDebugEnabled()) {
                logger.debug("Will return full list.");
            }
            logger.debug("Timer: " + (System.currentTimeMillis() - start));
            return importantTerms;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Will return sublist containing " + count + " of " + importantTerms.size() + " terms.");
            }

            logger.debug("Timer: " + (System.currentTimeMillis() - start));
            return importantTerms.subList(0, count);
        }
    }

    @SuppressWarnings("unchecked")
    public List<ImportantTerm> getImportantTerms(Long documentId, int howMany, boolean filterStopWords) {

        final Query query;

        if (filterStopWords) {
            query = this.em.createQuery("select dt from DocumentTerm dt join dt.document d join dt.term t "
                    + " where d.id = :documentId and t.valueLowercase NOT IN (:stopWords) "
                    + "   and dt.tfIdf is not null order by dt.tfIdf DESC");
            query.setParameter("stopWords", StopWords.ENGLISH);
        } else {
            query = this.em.createQuery("select dt from DocumentTerm dt join dt.document d join dt.term t "
                    + " where d.id = :documentId and dt.tfIdf is not null order by dt.tfIdf DESC");
        }
        query.setParameter("documentId", documentId);
        query.setMaxResults(howMany);

        final List<DocumentTerm> documentTerms = query.getResultList();

        float maxTfIdf = -1.0f;

        for (final DocumentTerm documentTerm : documentTerms) {
            if (documentTerm.getTfIdf() > maxTfIdf) {
                maxTfIdf = documentTerm.getTfIdf();
            }
        }

        final List<ImportantTerm> importantTerms = new ArrayList<ImportantTerm>(documentTerms.size());

        for (final DocumentTerm documentTerm : documentTerms) {
            importantTerms.add(new ImportantTerm(documentTerm.getTerm().getValueLowercase(), documentTerm.getCount(),
                    documentTerm.getTfIdf(), maxTfIdf));
        }

        return importantTerms;
    }

    public List<Document> listTopDocuments(Long userId, Date startDate, Date endDate, int count) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final Query query = this.em
                .createQuery("select d from Topic topic join topic.topicDocuments td join td.document d "
                        + "   where topic.userId = :userId "
                        + "     and td.creationDate > :startDate and td.creationDate < :endDate "
                        + "     and td.score > 0.2                                            "
                        + "     order by td.score desc");
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setFirstResult(0);
        query.setMaxResults(count);

        query.setLockMode(LockModeType.NONE);

        @SuppressWarnings("unchecked")
        final List<Document> range = query.getResultList();

        stopWatch.stop();
        logger.debug(stopWatch.toString());

        return range;
    }

    @Override
    public ScrollableResults scrollableSearch(Long userId, DocumentState state, String queryString,
            SortOrder sortOrder, Date startDate, Date endDate) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final FullTextQuery fullTextQuery = this.buildFullTextQuery(queryString, userId, startDate, endDate, false,
                state, FullTextQuery.THIS, FullTextQuery.SCORE);

        final Sort sort;
        switch (sortOrder) {
            case DATE_ASC:
                sort = new Sort(new SortField("id", SortField.LONG));
                break;
            case DATE_DESC:
                sort = new Sort(new SortField("id", SortField.LONG, true));
                break;
            case RELEVANCE:
                sort = new Sort(SortField.FIELD_SCORE, new SortField("id", SortField.LONG, true));
                break;
            default:
                throw new IllegalArgumentException("Unexpected SortOrder: " + sortOrder.name());
        }
        fullTextQuery.setSort(sort);

        fullTextQuery.setFetchSize(50);
        fullTextQuery.setReadOnly(true);
        fullTextQuery.setCacheable(false);
        fullTextQuery.setCacheMode(CacheMode.IGNORE);

        final ScrollableResults result = fullTextQuery.scroll(ScrollMode.FORWARD_ONLY);

        stopWatch.stop();
        logger.debug(stopWatch.toString());

        return result;
    }

    public DataRange<DocumentScore> searchByOwnerAndExpression(Long userId, String queryString, SortOrder sortOrder,
            Date startDate, Date endDate, int first, int count) {
        final boolean requireTopicsForUser = false;
        return searchWithAllOptions(userId, requireTopicsForUser, null, queryString, sortOrder, startDate, endDate,
                first, count);
    }

    public int searchResultCount(Long userId, DocumentState state, String queryString, Date startDate, Date endDate) {
        final DataRange<DocumentScore> range = searchByOwnerAndStateAndExpression(userId, state, queryString,
                startDate, endDate, 0, 1);
        return range.getTotalRowsAvailable();
    }

    public DataRange<DocumentScore> searchByOwnerAndStateAndExpression(Long userId, DocumentState state,
            String queryString, Date startDate, Date endDate, int first, int count) {
        final boolean requireTopicsForUser = false;
        return searchWithAllOptions(userId, requireTopicsForUser, state, queryString, null /* default */, startDate,
                endDate, first, count);
    }

    private DataRange<DocumentScore> searchWithAllOptions(final Long userId, final boolean requireTopicsForUser,
            final DocumentState state, final String queryString, final SortOrder sortOrder, final Date startDate,
            final Date endDate, final int first, final int count) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final FullTextQuery fullTextQuery = this.buildFullTextQuery(queryString, userId, startDate, endDate,
                requireTopicsForUser, state, FullTextQuery.THIS, FullTextQuery.SCORE);

        fullTextQuery.setFirstResult(first);
        fullTextQuery.setMaxResults(count);

        // optional sort order
        if (sortOrder == null || sortOrder == SortOrder.RELEVANCE) {
            final Sort defaultSort = new Sort(SortField.FIELD_SCORE, new SortField("id", SortField.LONG, true));
            fullTextQuery.setSort(defaultSort);
        } else if (sortOrder == SortOrder.DATE_DESC) {
            final Sort sort = new Sort(new SortField("creationDate", SortField.LONG, true));
            fullTextQuery.setSort(sort);
        } else if (sortOrder == SortOrder.DATE_ASC) {
            final Sort sort = new Sort(new SortField("creationDate", SortField.LONG));
            fullTextQuery.setSort(sort);
        }

        @SuppressWarnings("unchecked")
        final List<Object[]> results = fullTextQuery.list();
        final List<DocumentScore> range = new ArrayList<DocumentScore>(results.size());

        // copy to DocumentScore holder objects
        for (final Object[] ith : results) {
            final Document ithDoc = (Document) ith[0];
            final Float ithScore = (Float) ith[1];
            range.add(new DocumentScore(ithDoc, ithScore));
        }

        final int totalRows = fullTextQuery.getResultSize();
        final DataRange<DocumentScore> result = new DataRange<DocumentScore>(range, first, totalRows);

        stopWatch.stop();
        logger.debug(stopWatch.toString());
        return result;
    }

    private FullTextQuery buildFullTextQuery(final String queryString, final Long userId, final Date startDate,
            final Date endDate, final boolean requireTopicsForUser, final DocumentState state,
            final String... projectionConstants) {
        final String[] fields = new String[] { "title", "description", CONTENT_TEXT_FIELD_NAME, "author" };
        final MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_35, fields, new StandardAnalyzer(
                Version.LUCENE_35));

        org.apache.lucene.search.BooleanQuery compositeQuery = new org.apache.lucene.search.BooleanQuery();

        if (queryString != null) {
            org.apache.lucene.search.Query luceneTextQuery;
            try {
                luceneTextQuery = parser.parse(queryString);
                compositeQuery.add(luceneTextQuery, Occur.MUST);
            } catch (ParseException e) {
                throw new QueryException(e);
            }
        }

        org.apache.lucene.search.Query luceneSecurityQuery = NumericRangeQuery.newLongRange("user", userId, userId,
                true, true);
        compositeQuery.add(luceneSecurityQuery, Occur.MUST);

        if (startDate != null || endDate != null) {
            final Long startMillis = startDate == null ? 0 : startDate.getTime();
            final Long endMillis = endDate == null ? Long.MAX_VALUE : endDate.getTime();
            org.apache.lucene.search.Query dateRangeQuery = NumericRangeQuery.newLongRange("creationDate", startMillis,
                    endMillis, true, true);
            compositeQuery.add(dateRangeQuery, Occur.MUST);
        }

        if (requireTopicsForUser) {
            org.apache.lucene.search.Query topicUserQuery = NumericRangeQuery.newLongRange("topicUser", userId, userId,
                    true, true);
            compositeQuery.add(topicUserQuery, Occur.MUST);
        }

        if (state != null) {
            final int stateInt = state.getValue();
            org.apache.lucene.search.Query stateQuery = NumericRangeQuery.newIntRange("state", stateInt, stateInt,
                    true, true);
            compositeQuery.add(stateQuery, Occur.MUST);
        }

        final Session session = (Session) this.em.getDelegate();
        final FullTextSession fullTextSession = Search.getFullTextSession(session);
        final FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(compositeQuery).setProjection(
                projectionConstants);
        return fullTextQuery;
    }

    // FIXME - this arose after making documents shared by multiple users while
    // topics are still private. Users were seeing the names of other users'
    // topics. Tried filters, formulas, left joins, and about everything else I
    // could think of.
    // Left joins were especially troublesome as it seemed impossible to write
    // joins that would handle all 3 of the following scenarios:
    // 1.) No topics assigned to a document, 2.) No topics *for the current
    // user* assigned to a document, 3.) Topics assigned to current user.
    // Invariably, one of these 3 would be broken.
    // This is more performant than a lot of other options I thought of but
    // it still requires cirumventing what it seems a framework like Hibernate
    // or JPA should be able to provide. It is also not lazy-loadable and should
    // be used with great care on large collections or objects where collection
    // may not be read.
    public List<TopicDocument> getTopicDocuments(final Long documentId) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final Long userId = CurrentUserUtil.getCurrentUserId();

        final Query query = this.em.createQuery("select td " + "from TopicDocument td join td.topic topic "
                + "where td.document.id = :documentId and topic.userId = :userId " + "order by td.score desc");
        query.setParameter("userId", userId);
        query.setParameter("documentId", documentId);
        @SuppressWarnings("unchecked")
        final List<TopicDocument> filteredTopicDocuments = (List<TopicDocument>) query.getResultList();

        stopWatch.stop();
        logger.debug(stopWatch.toString());
        return filteredTopicDocuments;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.mothsoft.alexis.dao.DocumentDao#getRelatedTerms(java.lang.String,
     * java.lang.Long, int)
     */
    @SuppressWarnings("unchecked")
    public Graph getRelatedTerms(final String queryString, final Long userId, final int howMany) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final FullTextQuery fullTextQuery = this.buildFullTextQuery(queryString, userId, NO_DATE, NO_DATE, false,
                DocumentState.MATCHED_TO_TOPICS, FullTextQuery.ID);

        // find the specified number of terms from the most recent 100 documents
        // that match the query
        final Sort sort = new Sort(new SortField("creationDate", SortField.LONG, true));
        fullTextQuery.setSort(sort);
        fullTextQuery.setFirstResult(0);
        fullTextQuery.setMaxResults(100);

        final List<Long> documentIds = new ArrayList<Long>(100);
        final List<Long> termIds = new ArrayList<Long>(100);

        final List<Object[]> results = fullTextQuery.list();

        for (final Object[] ith : results) {
            final Long id = (Long) ith[0];
            documentIds.add(id);
        }

        final Map<String, Node> nodes = new LinkedHashMap<String, Node>();
        final Node root = new Node(queryString, Boolean.TRUE);
        nodes.put(queryString, root);

        final Map<String, Edge> edges = new HashMap<String, Edge>();

        if (!documentIds.isEmpty()) {
            final Session session = (Session) this.em.getDelegate();
            final org.hibernate.SQLQuery termsQuery = session.createSQLQuery("SELECT term.id "
                    + "        FROM document_term dt INNER JOIN term on term.id = dt.term_id "
                    + "        WHERE dt.document_id IN (:documentIds) GROUP BY term.id ORDER BY SUM(dt.tf_idf) DESC");
            termsQuery.setParameterList("documentIds", documentIds);
            termsQuery.setMaxResults(100);
            termIds.addAll((List<Long>) termsQuery.list());
        }

        if (!documentIds.isEmpty() && !termIds.isEmpty()) {

            final Session session = (Session) this.em.getDelegate();
            final org.hibernate.SQLQuery associationsQuery = session
                    .createSQLQuery("SELECT CONCAT(a.term_value) term_a_value, CONCAT(b.term_value) term_b_value, SUM(da.association_weight) sum_weight "
                            + "      FROM document_association da "
                            + "      INNER JOIN term a ON da.term_a_id = a.id "
                            + "        AND a.part_of_speech NOT IN (1, 3, 18, 19, 25, 39, 40) "
                            + "        AND length(a.term_value) > 2 "
                            + "      INNER JOIN term b ON da.term_b_id = b.id "
                            + "        AND b.part_of_speech NOT IN (1, 3, 18, 19, 25, 39, 40) "
                            + "        AND length(b.term_value) > 2 "
                            + "      WHERE da.document_id IN (:documentIds) AND (da.term_a_id IN (:termIds) OR da.term_b_id IN (:termIds)) "
                            + "      GROUP BY a.id, b.id ORDER BY sum_weight DESC");
            associationsQuery.setParameterList("documentIds", documentIds);
            associationsQuery.setParameterList("termIds", termIds);
            associationsQuery.setMaxResults(howMany);

            final List<Object[]> relatedTermsResults = associationsQuery.list();

            final Set<String> aNodeKeys = new HashSet<String>();
            final Set<String> bNodeKeys = new HashSet<String>();

            for (final Object[] ith : relatedTermsResults) {
                final String a = (String) ith[0];
                final String b = (String) ith[1];

                if (!nodes.containsKey(a)) {
                    final Node node = new Node(a);
                    nodes.put(a, node);
                }

                if (!nodes.containsKey(b)) {
                    final Node node = new Node(b);
                    nodes.put(b, node);
                }

                if (a.equals(b)) {
                    continue;
                }

                final String edgeKey = a + "||" + b;
                final String edgeKeyInverse = b + "||" + a;
                if (!edges.containsKey(edgeKey) && !edges.containsKey(edgeKeyInverse)) {
                    final Node nodeA = nodes.get(a);
                    final Node nodeB = nodes.get(b);

                    aNodeKeys.add(a);
                    bNodeKeys.add(b);

                    final Edge edge = new Edge(nodeA, nodeB);
                    edges.put(edgeKey, edge);
                }
            }

            // "orphan" handling, any b that is not also an a needs an edge from
            // root
            final Set<String> orphanKeys = new HashSet<String>();
            orphanKeys.addAll(bNodeKeys);
            orphanKeys.removeAll(aNodeKeys);

            for (final String orphanKey : orphanKeys) {
                final Node orphan = nodes.get(orphanKey);
                final Edge orphanToParent = new Edge(root, orphan);
                edges.put(root.getName() + "||" + orphan.getName(), orphanToParent);
            }
        }

        final List<Node> nodeList = new ArrayList<Node>(nodes.size());
        // keep root as first element
        nodes.remove(root.getName());
        nodeList.add(root);
        nodeList.addAll(nodes.values());

        final Graph graph = new Graph(nodeList, new ArrayList<Edge>(edges.values()));

        stopWatch.stop();
        logger.info("Related terms search took: " + stopWatch.toString());

        return graph;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ImportantNamedEntity> getImportantNamedEntities(Long userId, Date startDate, Date endDate, int howMany) {
        final Query query = this.em
                .createQuery("SELECT NEW com.mothsoft.alexis.domain.ImportantNamedEntity(ne.name, sum(ne.count)) "
                        + "FROM DocumentNamedEntity ne JOIN ne.document document JOIN document.documentUsers documentUser "
                        + "WHERE document.creationDate >= :startDate AND document.creationDate <= :endDate AND documentUser.user.id = :userId "
                        + "GROUP BY ne.name ORDER BY sum(ne.count) DESC");
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setParameter("userId", userId);
        query.setMaxResults(howMany);
        return query.getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ImportantNamedEntity> getImportantNamedEntitiesForDocument(Long documentId, int howMany) {
        final Query query = this.em
                .createQuery("SELECT NEW com.mothsoft.alexis.domain.ImportantNamedEntity(ne.name, sum(ne.count)) "
                        + "FROM DocumentNamedEntity ne JOIN ne.document document WHERE document.id = :documentId "
                        + "GROUP BY ne.name ORDER BY sum(ne.count) DESC");
        query.setParameter("documentId", documentId);
        query.setMaxResults(howMany);
        return query.getResultList();
    }

    private class Tuple<T1, T2> {
        public T1 t1;
        public T2 t2;
    }
}
