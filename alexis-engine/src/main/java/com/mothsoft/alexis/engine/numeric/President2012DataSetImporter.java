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
package com.mothsoft.alexis.engine.numeric;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.io.IOUtils;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import twitter4j.internal.logging.Logger;

import com.ibm.icu.util.GregorianCalendar;
import com.mothsoft.alexis.dao.DataSetDao;
import com.mothsoft.alexis.dao.DataSetPointDao;
import com.mothsoft.alexis.dao.DataSetTypeDao;
import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.domain.DataSetPoint;
import com.mothsoft.alexis.domain.DataSetType;
import com.mothsoft.alexis.util.HttpClientResponse;
import com.mothsoft.alexis.util.NetworkingUtil;

public class President2012DataSetImporter implements DataSetImporter {

    private static final Logger logger = Logger.getLogger(President2012DataSetImporter.class);

    private static final String BASE_URL = "http://elections.huffingtonpost.com/pollster/api/polls.xml?topic=2012-president&state=US&sort=updated&after=%s&page=%d";

    private static final String END_DATE = "end_date";

    private static final String YYYY_MM_DD = "yyyy-MM-dd";

    private static final String ANCESTOR_POLL = "ancestor::poll";

    private static final String CHOICE = "choice";

    private static final String VALUE = "value";

    private static final String XPATH_QUESTIONS = "//responses/response[choice/text() = 'Obama' or choice/text() = 'Romney']/ancestor::question";

    private static final String XPATH_RESPONSE_FROM_QUESTION = ".//response";

    private static final String POLLING_DATA = "Polling Data";

    private static final String BASE_DATA_SET_NAME = "2012 Presidential Election - %s";

    private static final Set<String> CANDIDATE_OPTIONS;

    static {
        CANDIDATE_OPTIONS = new HashSet<String>();
        CANDIDATE_OPTIONS.add("Obama");
        CANDIDATE_OPTIONS.add("Romney");
        CANDIDATE_OPTIONS.add("Undecided");
    }

    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;
    private DataSetDao dataSetDao;
    private DataSetPointDao dataSetPointDao;
    private DataSetTypeDao dataSetTypeDao;

    @PersistenceContext
    private EntityManager em;

    public President2012DataSetImporter() {
        super();
        logger.info("Initialized");
    }

    public void setTransactionManager(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.transactionTemplate = new TransactionTemplate(this.transactionManager);
    }

    public void setDataSetDao(final DataSetDao dataSetDao) {
        this.dataSetDao = dataSetDao;
    }

    public void setDataSetPointDao(final DataSetPointDao dataSetPointDao) {
        this.dataSetPointDao = dataSetPointDao;
    }

    public void setDataSetTypeDao(final DataSetTypeDao dataSetTypeDao) {
        this.dataSetTypeDao = dataSetTypeDao;
    }

    @Override
    public void importData() {
        logger.info("Importing poll data");

        // look back 7 days to allow for updates to polls that have ended but
        // not completely been tallied/finalized
        final GregorianCalendar afterCalendar = new GregorianCalendar();
        afterCalendar.set(Calendar.HOUR_OF_DAY, 0);
        afterCalendar.set(Calendar.MINUTE, 0);
        afterCalendar.set(Calendar.MILLISECOND, 0);
        afterCalendar.add(Calendar.DAY_OF_MONTH, -7);

        final SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD);
        final String after = format.format(afterCalendar.getTime());

        int pageNumber = 1;
        Map<Date, PollResults> pollResultsByDate = new HashMap<Date, PollResults>();

        Map<Date, PollResults> lastPage = getPage(after, pageNumber);
        while (!lastPage.isEmpty()) {

            // save last to map
            for (final Map.Entry<Date, PollResults> entry : lastPage.entrySet()) {
                final Date date = entry.getKey();
                final PollResults pr = entry.getValue();
                append(date, pr, pollResultsByDate);
            }

            lastPage = getPage(after, ++pageNumber);
        }

        if (!pollResultsByDate.isEmpty()) {
            logger.info("Deleting existing points after: " + after);
            deletePoints(afterCalendar.getTime());
        }

        save(pollResultsByDate);
    }

    private void deletePoints(final Date after) {
        this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus txStatus) {
                final Set<DataSet> dataSets = new HashSet<DataSet>();

                for (final String choice : CANDIDATE_OPTIONS) {
                    final DataSet dataSet = findOrCreateDataSet(choice);
                    dataSets.add(dataSet);
                }

                final String queryString = "DELETE FROM DataSetPoint p WHERE p.dataSet IN :dataSets AND p.x >= :after";
                final Query query = President2012DataSetImporter.this.em.createQuery(queryString);
                query.setParameter("dataSets", dataSets);
                query.setParameter("after", after);
                final int affected = query.executeUpdate();
                logger.info("Affected " + affected + " rows");
            }
        });
    }

    private static void append(final Date date, final PollResults pr, final Map<Date, PollResults> consolidated) {
        if (consolidated.containsKey(date)) {
            final PollResults cpr = consolidated.get(date);

            // append the results
            for (final String choice : pr.getAvailableChoices()) {
                cpr.tally(choice, pr.valueOf(choice));
            }

        } else {
            // use the only value we have as-is
            consolidated.put(date, pr);
        }
    }

    private void save(final Map<Date, PollResults> pollResultsByDate) {
        this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus txStatus) {
                final Map<String, DataSet> dataSets = new HashMap<String, DataSet>();

                for (final Map.Entry<Date, PollResults> entry : pollResultsByDate.entrySet()) {
                    final Date date = entry.getKey();
                    logger.debug("Date: " + date.toString());

                    final PollResults pollResults = entry.getValue();

                    for (final String choice : pollResults.getAvailableChoices()) {
                        // use data set if cached
                        DataSet dataSet = dataSets.get(choice);

                        // find or create the data set
                        if (dataSet == null) {
                            dataSet = findOrCreateDataSet(choice);
                            dataSets.put(choice, dataSet);
                        }

                        logger.debug(String.format("%s => %f", choice, pollResults.valueOf(choice)));

                        // save the value
                        final DataSetPoint point = new DataSetPoint(dataSet, date, pollResults.valueOf(choice));
                        President2012DataSetImporter.this.dataSetPointDao.add(point);
                    }
                }
            }

        });
    }

    private DataSet findOrCreateDataSet(final String choice) {
        final DataSetType type = President2012DataSetImporter.this.dataSetTypeDao.findSystemDataSetType(POLLING_DATA);

        final String name = String.format(BASE_DATA_SET_NAME, choice);

        DataSet dataSet = President2012DataSetImporter.this.dataSetDao.findSystemDataSet(type, name);

        if (dataSet == null) {
            dataSet = new DataSet(name, type);
            President2012DataSetImporter.this.dataSetDao.add(dataSet);
        }

        return dataSet;

    }

    @SuppressWarnings("unchecked")
    private Map<Date, PollResults> getPage(final String after, final int pageNumber) {
        final Map<Date, PollResults> pageMap = new LinkedHashMap<Date, PollResults>();

        HttpClientResponse httpResponse = null;

        try {
            final URL url = new URL(String.format(BASE_URL, after, pageNumber));
            httpResponse = NetworkingUtil.get(url, null, null);

            final SAXReader saxReader = new SAXReader();

            org.dom4j.Document document;
            try {
                document = saxReader.read(httpResponse.getInputStream());
            } catch (DocumentException e) {
                throw new IOException(e);
            } finally {
                IOUtils.closeQuietly(httpResponse);
            }

            final SimpleDateFormat format = new SimpleDateFormat(YYYY_MM_DD);

            final List<Node> questions = document.selectNodes(XPATH_QUESTIONS);
            for (final Node question : questions) {
                final Date endDate;
                final Node poll = question.selectSingleNode(ANCESTOR_POLL);
                final Node endDateNode = poll.selectSingleNode(END_DATE);
                try {
                    endDate = format.parse(endDateNode.getText());
                    logger.debug(String.format("%s: %s", END_DATE, format.format(endDate)));
                } catch (final ParseException e) {
                    throw new RuntimeException(e);
                }

                final List<Node> responses = question.selectNodes(XPATH_RESPONSE_FROM_QUESTION);
                for (final Node response : responses) {
                    final Node choiceNode = response.selectSingleNode(CHOICE);
                    final String choice = choiceNode.getText();

                    if (President2012DataSetImporter.CANDIDATE_OPTIONS.contains(choice)) {
                        final Node valueNode = response.selectSingleNode(VALUE);
                        final Double value = Double.valueOf(valueNode.getText());
                        append(pageMap, endDate, choice, value);
                    }
                }
            }

            httpResponse.close();
            httpResponse = null;

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }

        return pageMap;
    }

    private void append(final Map<Date, PollResults> pageMap, final Date date, final String text, final Double value) {
        if (!pageMap.containsKey(date)) {
            pageMap.put(date, new PollResults());
        }

        final PollResults results = pageMap.get(date);
        results.tally(text, value);
    }

    private class PollResults {
        private Map<String, Double> resultMap = new LinkedHashMap<String, Double>(8);
        private Map<String, Integer> countMap = new LinkedHashMap<String, Integer>(8);

        Set<String> getAvailableChoices() {
            return resultMap.keySet();
        }

        void tally(final String choice, final Double value) {
            final Double currentValue = resultMap.containsKey(choice) ? resultMap.get(choice) : 0.0d;
            final Integer currentCount = countMap.containsKey(choice) ? countMap.get(choice) : 0;

            resultMap.put(choice, value + currentValue);
            countMap.put(choice, 1 + currentCount);
        }

        Double valueOf(final String choice) {
            final Double currentValue = resultMap.containsKey(choice) ? resultMap.get(choice) : 0.0d;
            final Integer currentCount = countMap.containsKey(choice) ? countMap.get(choice) : 0;

            if (currentCount == 0) {
                return (double) 0;
            } else {
                return (double) currentValue / (double) currentCount;
            }
        }

    }
}
