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
package com.mothsoft.alexis.engine.textual;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.engine.Task;

public class LuceneIndexerTask implements Task {

    private static final Logger logger = Logger.getLogger(LuceneIndexerTask.class);

    private static final int BATCH_SIZE = 50;

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings("unchecked")
    @Transactional
    public void execute() {
        final long start = System.currentTimeMillis();
        logger.info("Starting Lucene indexer...");

        final Session session = (Session) this.em.getDelegate();
        final FullTextSession fullTextSession = Search.getFullTextSession(session);

        int first = 0;

        final Query query = session.createQuery("FROM Document d WHERE d.indexed = false");
        query.setFirstResult(first);
        query.setMaxResults(BATCH_SIZE);

        List<Document> documents = query.list();

        while (!documents.isEmpty()) {
            for (final Document document : documents) {
                document.setIndexed(true);
                fullTextSession.update(document);
                fullTextSession.index(document);
            }

            // next
            first += BATCH_SIZE;
            documents = query.list();
        }

        logger.info("Lucene indexing took: " + ((System.currentTimeMillis() - start) / 1000.00) + " seconds");
    }
}
