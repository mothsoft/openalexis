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
//package com.mothsoft.alexis.engine.predictive;
//
//import java.io.File;
//import java.io.IOException;
//import java.sql.Timestamp;
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//import java.util.Map;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//
//import opennlp.maxent.GIS;
//import opennlp.maxent.GISModel;
//import opennlp.maxent.io.GISModelWriter;
//import opennlp.maxent.io.SuffixSensitiveGISModelWriter;
//import opennlp.model.DataIndexer;
//import opennlp.model.Event;
//import opennlp.model.EventStream;
//import opennlp.model.TwoPassDataIndexer;
//
//import org.apache.commons.lang.time.StopWatch;
//import org.apache.log4j.Logger;
//import org.hibernate.ScrollableResults;
//import org.hibernate.Session;
//import org.hibernate.ejb.HibernateEntityManager;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.TransactionDefinition;
//import org.springframework.transaction.TransactionStatus;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.support.DefaultTransactionDefinition;
//import org.springframework.transaction.support.TransactionCallback;
//import org.springframework.transaction.support.TransactionCallbackWithoutResult;
//import org.springframework.transaction.support.TransactionTemplate;
//
//import com.mothsoft.alexis.dao.DataSetPointDao;
//import com.mothsoft.alexis.dao.DocumentDao;
//import com.mothsoft.alexis.dao.ModelDao;
//import com.mothsoft.alexis.domain.DataSetPoint;
//import com.mothsoft.alexis.domain.Document;
//import com.mothsoft.alexis.domain.DocumentState;
//import com.mothsoft.alexis.domain.Model;
//import com.mothsoft.alexis.domain.ModelState;
//import com.mothsoft.alexis.domain.SortOrder;
//import com.mothsoft.alexis.domain.TimeUnits;
//import com.mothsoft.alexis.engine.Task;
//
//public class OpenNLPMaxentModelTrainerTask extends AbstractModelTrainer implements ModelTrainer, Task {
//
//    private static final Logger logger = Logger.getLogger(OpenNLPMaxentModelTrainerTask.class);
//
//    private static final String OUTCOME_FORMAT = "+%d%s:%f";
//    private static final String BIN_GZ_EXT = ".bin.gz";
//
//    private DataSetPointDao dataSetPointDao;
//    private DocumentDao documentDao;
//    private ModelDao modelDao;
//    private TransactionTemplate transactionTemplate;
//    private File baseDirectory;
//    private int iterations;
//    private int cutoff;
//
//    @PersistenceContext
//    private EntityManager em;
//
//    public OpenNLPMaxentModelTrainerTask() {
//        super();
//    }
//
//    public void setBaseDirectory(File baseDirectory) {
//        this.baseDirectory = baseDirectory;
//    }
//
//    public void setDataSetPointDao(DataSetPointDao dataSetPointDao) {
//        this.dataSetPointDao = dataSetPointDao;
//    }
//
//    public void setDocumentDao(final DocumentDao documentDao) {
//        this.documentDao = documentDao;
//    }
//
//    public void setModelDao(ModelDao modelDao) {
//        this.modelDao = modelDao;
//    }
//
//    public void setTransactionManager(final PlatformTransactionManager transactionManager) {
//        final TransactionDefinition transactionDefinition = new DefaultTransactionDefinition(
//                DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
//        this.transactionTemplate = new TransactionTemplate(transactionManager, transactionDefinition);
//    }
//
//    public void setIterations(int iterations) {
//        this.iterations = iterations;
//    }
//
//    public void setCutoff(int cutoff) {
//        this.cutoff = cutoff;
//    }
//
//    protected DocumentDao getDocumentDao() {
//        return this.documentDao;
//    }
//
//    @Transactional
//    @Override
//    public void execute() {
//        final Long modelId = findAndMark();
//
//        if (modelId != null) {
//            logger.info(String.format("Training model %d", modelId));
//
//            final StopWatch stopWatch = new StopWatch();
//            stopWatch.start();
//            final Model model = this.modelDao.get(modelId);
//            train(model);
//            stopWatch.stop();
//
//            logger.info(String.format("Training model %d took: %s", modelId, stopWatch.toString()));
//        }
//    }
//
//    @Override
//    public void train(Model model) {
//        final int lookahead = model.getLookahead();
//        final TimeUnits timeUnits = model.getTimeUnits();
//        final long durationOfUnit = timeUnits.getDuration();
//
//        final Date startDate = TimeUnits.floor(model.getStartDate(), timeUnits);
//        final Date endDate = TimeUnits.ceil(model.getEndDate(), timeUnits);
//
//        final Timestamp startDatePoints = new Timestamp(startDate.getTime() - durationOfUnit);
//        final Timestamp endDatePoints = new Timestamp(endDate.getTime() + (lookahead * durationOfUnit));
//
//        List<DataSetPoint> points = this.dataSetPointDao.findAndAggregatePointsGroupedByUnit(
//                model.getTrainingDataSet(), startDatePoints, endDatePoints, timeUnits);
//        Map<Date, DataSetPoint> pointMap = toMap(points);
//
//        final Map<Date, Float> percentChangeMap = calculatePercentChange(points, pointMap);
//
//        // should release these collections once percent change is calculated
//        points = null;
//        pointMap = null;
//
//        final HibernateEntityManager hem = this.em.unwrap(HibernateEntityManager.class);
//        final Session session = hem.getSession();
//
//        final Long userId = model.getUserId();
//        final DocumentState state = null;
//        final String queryString = model.getTopic().getSearchExpression();
//        final ScrollableResults scrollableResults = this.documentDao.scrollableSearch(userId, state, queryString,
//                SortOrder.DATE_ASC, startDatePoints, endDatePoints);
//
//        try {
//            final EventStream eventStream = new DocumentScoreEventStream(model, scrollableResults, session,
//                    percentChangeMap);
//            final DataIndexer dataIndexer = new TwoPassDataIndexer(eventStream, this.cutoff);
//
//            if (!logger.isDebugEnabled()) {
//                GIS.PRINT_MESSAGES = false;
//            }
//
//            logger.debug("Invoking GIS.trainModel");
//            final GISModel gisModel = GIS.trainModel(this.iterations, dataIndexer);
//            logger.debug("GIS.trainModel complete");
//
//            // because we've been clearing the entity manager's session
//            model = this.modelDao.get(model.getId());
//            writeModelToFile(model, gisModel);
//            logger.info("Created model: " + gisModel);
//            model.onTrainingComplete();
//
//        } catch (final OutOfMemoryError e) {
//            logError(model.getId(), e);
//            throw e;
//        } catch (final Exception e) {
//            logError(model.getId(), e);
//        } finally {
//            scrollableResults.close();
//        }
//    }
//
//    private void logError(final Long modelId, final Throwable t) {
//        this.transactionTemplate.execute(new TransactionCallbackWithoutResult() {
//
//            @Override
//            protected void doInTransactionWithoutResult(final TransactionStatus status) {
//                logger.error("Model " + modelId + " training failed: " + t, t);
//                final Model model = OpenNLPMaxentModelTrainerTask.this.modelDao.get(modelId);
//                model.setState(ModelState.ERROR);
//            }
//        });
//    }
//
//    private void writeModelToFile(final Model model, final GISModel gisModel) throws IOException {
//        final File userFile = new File(this.baseDirectory, "" + model.getUserId());
//        userFile.mkdirs();
//        final File file = new File(userFile, model.getId() + BIN_GZ_EXT);
//        final GISModelWriter writer = new SuffixSensitiveGISModelWriter(gisModel, file);
//
//        try {
//            logger.debug("Calling GISModelWriter.persist");
//            writer.persist();
//            logger.debug("GISModelWriter.persist complete");
//        } finally {
//            logger.debug("Calling GISModelWriter.close");
//            writer.close();
//            logger.debug("GISModelWriter closed");
//        }
//    }
//
//    /**
//     * Mark model as training in a separate transaction to ensure external
//     * visibility
//     * 
//     * @return - id of marked model
//     */
//    private Long findAndMark() {
//        return this.transactionTemplate.execute(new TransactionCallback<Long>() {
//            @Override
//            public Long doInTransaction(TransactionStatus txStatus) {
//                return OpenNLPMaxentModelTrainerTask.this.modelDao.findAndMarkOne(ModelState.PENDING,
//                        ModelState.TRAINING);
//            }
//        });
//    }
//
//    private class DocumentScoreEventStream implements EventStream {
//
//        private static final int BATCH_SIZE = 25;
//
//        private final Model model;
//        private final Map<Date, Float> percentChangeMap;
//
//        private final ScrollableResults scrollableResults;
//        private final Session session;
//
//        private int documentNumber = 0;
//        private int pendingOutcomes = 0;
//        private Document doc = null;
//        private String[] context = new String[0];
//        private float[] values = new float[0];
//
//        public DocumentScoreEventStream(final Model model, final ScrollableResults scrollableResults,
//                final Session session, final Map<Date, Float> percentChangeMap) {
//            this.model = model;
//            this.scrollableResults = scrollableResults;
//            this.session = session;
//            this.percentChangeMap = percentChangeMap;
//        }
//
//        @Override
//        public Event next() throws IOException {
//            final Event event;
//
//            if (pendingOutcomes == 0) {
//                pendingOutcomes = model.getLookahead();
//
//                // [Document][Float]
//                final Object[] object = this.scrollableResults.get();
//                doc = (Document) object[0];
//                documentNumber++;
//
//                final Map<String, Integer> contextMap;
//
//                // handle unusual case of stale index. Would be nice to fix...
//                if (doc == null) {
//                    logger.warn("Can't find document number " + documentNumber + "; index stale?");
//                    contextMap = Collections.emptyMap();
//                } else {
//                    contextMap = OpenNLPMaxentContextBuilder.buildContext(doc);
//                }
//
//                context = new String[contextMap.size()];
//                values = new float[contextMap.size()];
//                OpenNLPMaxentContextBuilder.buildContextArrays(contextMap, context, values);
//            }
//
//            if (doc == null) {
//                event = new Event("MISSING_DOCUMENT", context, values);
//            } else {
//                final String outcome = buildOutcome(model, model.getLookahead() - pendingOutcomes,
//                        doc.getCreationDate(), percentChangeMap);
//                event = new Event(outcome, context, values);
//                pendingOutcomes--;
//
//                if (logger.isDebugEnabled()) {
//                    logger.debug(String.format("Event for model: %d, document ID: %d, outcome: %s", model.getId(),
//                            doc.getId(), outcome));
//                }
//            }
//
//            // clear out every BATCH_SIZE documents
//            if (pendingOutcomes == 0 && documentNumber % BATCH_SIZE == 0) {
//                session.flush();
//                session.clear();
//            }
//
//            return event;
//        }
//
//        private String buildOutcome(final Model model, final int i, final Date creationDate,
//                final Map<Date, Float> percentChangeMap) {
//
//            final TimeUnits timeUnits = model.getTimeUnits();
//            final Date baseTime = TimeUnits.floor(creationDate, timeUnits);
//            final Date time = new Date(baseTime.getTime() + (i * timeUnits.getDuration()));
//
//            double pctChange;
//
//            if (percentChangeMap.containsKey(time)) {
//                pctChange = percentChangeMap.get(time);
//            } else {
//                pctChange = 0.0d;
//            }
//
//            final double absPctChange = Math.abs(pctChange);
//
//            // need discrete values
//            for (int j = 0; j < Model.OUTCOME_ARRAY.length; j++) {
//                if (absPctChange > Model.OUTCOME_ARRAY[j] && absPctChange <= Model.OUTCOME_ARRAY[j + 1]) {
//                    final double closest = Model.OUTCOME_ARRAY[j];
//                    pctChange = pctChange < 0.0d ? -1 * closest : closest;
//                    break;
//                }
//            }
//
//            final String outcome = String.format(OUTCOME_FORMAT, i, timeUnits.name(), pctChange);
//
//            return outcome;
//        }
//
//        @Override
//        public boolean hasNext() throws IOException {
//            return scrollableResults.next() || pendingOutcomes > 0;
//        }
//    }
//}
