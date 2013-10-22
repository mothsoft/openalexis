///*   Copyright 2012 Tim Garrett, Mothsoft LLC
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//package com.mothsoft.alexis.engine.textual;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import javax.persistence.Query;
//
//import org.apache.log4j.Logger;
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.index.Term;
//import org.hibernate.Session;
//import org.hibernate.search.FullTextSession;
//import org.hibernate.search.Search;
//import org.hibernate.search.SearchFactory;
//import org.hibernate.search.indexes.IndexReaderAccessor;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.mothsoft.alexis.domain.Document;
//import com.mothsoft.alexis.domain.DocumentAssociation;
//import com.mothsoft.alexis.domain.DocumentTerm;
//import com.mothsoft.alexis.domain.TFIDF;
//import com.mothsoft.alexis.engine.Task;
//
///**
// * Calculate the TF-IDF and create dependent summary data
// * 
// * @author tgarrett
// * 
// */
//public class TFIDFCalculatorImpl implements Task {
//
//    private static final Logger logger = Logger.getLogger(TFIDFCalculatorImpl.class);
//
//    private static final String CONTENT_TEXT_FIELD_NAME = "content.text";
//
//    @PersistenceContext
//    private EntityManager em;
//
//    public TFIDFCalculatorImpl() throws IOException {
//    }
//
//    public void setEm(EntityManager em) {
//        this.em = em;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Transactional
//    public void execute() {
//        final long start = System.currentTimeMillis();
//
//        final FullTextSession fullTextSession = Search.getFullTextSession((Session) this.em.getDelegate());
//        final SearchFactory searchFactory = fullTextSession.getSearchFactory();
//        final IndexReaderAccessor ira = searchFactory.getIndexReaderAccessor();
//        final IndexReader reader = ira.open(com.mothsoft.alexis.domain.Document.class);
//
//        final Query query = em
//                .createQuery("select d from Document d join d.documentTerms dt where dt.tfIdf IS NULL ORDER BY d.id ASC");
//        final List<Document> documents = query.getResultList();
//
//        final Term luceneTerm = new Term(CONTENT_TEXT_FIELD_NAME);
//        int affectedRows = 0;
//
//        try {
//            for (final Document document : documents) {
//                final Map<String, Float> termTfIdfMap = new HashMap<String, Float>();
//
//                // calculate term TF-IDFs
//                for (final DocumentTerm documentTerm : document.getDocumentTerms()) {
//                    final Term term = luceneTerm.createTerm(documentTerm.getTerm().getValueLowercase());
//                    Float score = TFIDF.score(documentTerm.getTerm().getValueLowercase(), documentTerm.getCount(),
//                            document.getTermCount(), reader.numDocs(), reader.docFreq(term));
//                    documentTerm.setTfIdf(score);
//                    termTfIdfMap.put(documentTerm.getTerm().getValueLowercase(), score);
//                    affectedRows++;
//                }
//
//                // update association weights
//                for (final DocumentAssociation documentAssociation : document.getDocumentAssociations()) {
//                    final String a = documentAssociation.getA().getValueLowercase();
//                    final String b = documentAssociation.getB().getValueLowercase();
//                    documentAssociation.setAssociationWeight((float) documentAssociation.getAssociationCount()
//                            * (termTfIdfMap.get(a) + termTfIdfMap.get(b)));
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            ira.close(reader);
//        }
//
//        logger.info("TF-IDF calc took: " + ((System.currentTimeMillis() - start) / 1000.00) + " seconds and affected "
//                + affectedRows + " rows.");
//    }
//
//}
