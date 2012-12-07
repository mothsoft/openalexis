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
package com.mothsoft.alexis.engine.predictive;

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.maxent.io.SuffixSensitiveGISModelReader;
import opennlp.model.AbstractModel;
import opennlp.model.MaxentModel;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.hibernate.ScrollableResults;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.mothsoft.alexis.dao.DataSetPointDao;
import com.mothsoft.alexis.dao.DocumentDao;
import com.mothsoft.alexis.dao.ModelDao;
import com.mothsoft.alexis.domain.DataSetPoint;
import com.mothsoft.alexis.domain.Document;
import com.mothsoft.alexis.domain.Model;
import com.mothsoft.alexis.domain.ModelState;
import com.mothsoft.alexis.domain.ModelType;
import com.mothsoft.alexis.domain.SortOrder;
import com.mothsoft.alexis.domain.TimeUnits;
import com.mothsoft.alexis.engine.Task;

public class OpenNLPMaxentModelExecutorTask implements Task {

    private static final Logger logger = Logger.getLogger(OpenNLPMaxentModelExecutorTask.class);

    private static final Pattern OUTCOME_PATTERN = Pattern.compile("\\+(\\d+)(\\S+)\\:(\\S+)");
    private static final String BIN_GZ_EXT = ".bin.gz";

    private File baseDirectory;
    private DataSetPointDao dataSetPointDao;
    private DocumentDao documentDao;
    private ModelDao modelDao;
    private TransactionTemplate transactionTemplate;

    public OpenNLPMaxentModelExecutorTask() {
        super();
    }

    public void setBaseDirectory(File baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public void setDataSetPointDao(final DataSetPointDao dataSetPointDao) {
        this.dataSetPointDao = dataSetPointDao;
    }

    public void setDocumentDao(DocumentDao documentDao) {
        this.documentDao = documentDao;
    }

    public void setModelDao(final ModelDao modelDao) {
        this.modelDao = modelDao;
    }

    public void setTransactionManager(final PlatformTransactionManager transactionManager) {
        final TransactionDefinition transactionDefinition = new DefaultTransactionDefinition(
                DefaultTransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionTemplate = new TransactionTemplate(transactionManager, transactionDefinition);
    }

    @Override
    public void execute() {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final List<Long> modelIds = findModelsToExecute();
        final int size = modelIds.size();
        logger.info(String.format("Found %d models in state READY", size));

        int executed = 0;

        for (final Long modelId : modelIds) {
            boolean success = execute(modelId);
            if (success) {
                executed++;
            }
        }

        stopWatch.stop();
        logger.info(String.format("Executed %d of %d models, took: %s", executed, size, stopWatch.toString()));
    }

    private List<Long> findModelsToExecute() {
        return this.transactionTemplate.execute(new TransactionCallback<List<Long>>() {
            @Override
            public List<Long> doInTransaction(TransactionStatus arg0) {
                final List<Model> models = OpenNLPMaxentModelExecutorTask.this.modelDao.findByTypeAndState(
                        ModelType.MAXENT, ModelState.READY);
                final List<Long> modelIds = new ArrayList<Long>(models.size());

                for (final Model model : models) {
                    modelIds.add(model.getId());
                }

                return modelIds;
            }
        });
    }

    private boolean execute(final Long modelId) {
        return this.transactionTemplate.execute(new TransactionCallback<Boolean>() {
            @Override
            public Boolean doInTransaction(TransactionStatus arg0) {
                final Model model = OpenNLPMaxentModelExecutorTask.this.modelDao.get(modelId);
                return OpenNLPMaxentModelExecutorTask.this.doExecute(model);
            }
        });
    }

    private boolean doExecute(final Model model) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        boolean result = false;

        try {
            logger.info(String.format("Executing model %d", model.getId()));

            // load model file
            final File userDirectory = new File(baseDirectory, "" + model.getUserId());
            final File modelFile = new File(userDirectory, model.getId() + BIN_GZ_EXT);
            final AbstractModel maxentModel = new SuffixSensitiveGISModelReader(modelFile).getModel();

            final Date now = new Date();
            final TimeUnits timeUnits = model.getTimeUnits();
            final Timestamp topOfPeriod = new Timestamp(TimeUnits.floor(now, timeUnits).getTime());
            final Timestamp endOfPeriod = new Timestamp(topOfPeriod.getTime() + timeUnits.getDuration() - 1);

            // first position: sum of changes predicted, second position: number
            // of samples--will calculate a boring old mean...
            final double[][] changeByPeriod = new double[model.getLookahead()][2];

            // initialize
            for (int i = 0; i < changeByPeriod.length; i++) {
                changeByPeriod[i][0] = 0.0d;
                changeByPeriod[i][1] = 0.0d;
            }

            // find the most recent point value
            // FIXME - some sparse data sets may require executing the model on
            // all documents since that point or applying some sort of
            // dead-reckoning logic for smoothing
            final DataSetPoint initial = this.dataSetPointDao.findLastPointBefore(model.getTrainingDataSet(),
                    endOfPeriod);

            // let's get the corner cases out of the way
            if (initial == null) {
                logger.warn("Insufficient data to execute model!");
                return false;
            }

            // happy path
            // build consolidated context of events in this period
            // find current value of training data set for this period
            final double[] probs = eval(model, topOfPeriod, endOfPeriod, maxentModel);

            // predict from the last available point, adjusted for time
            // remaining in period
            final double y0 = initial.getY();

            // map outcomes to periods in the future (at least no earlier than
            // this period)
            for (int i = 0; i < probs.length; i++) {
                // in the form +nU:+/-x, where n is the number of periods, U is
                // the unit type for the period, +/- is the direction, and x is
                // a discrete value from Model.OUTCOME_ARRAY
                final String outcome = maxentModel.getOutcome(i);

                final Matcher matcher = OUTCOME_PATTERN.matcher(outcome);

                if (!matcher.matches()) {
                    logger.warn("Can't process outcome: " + outcome + "; skipping");
                    continue;
                }

                final int period = Integer.valueOf(matcher.group(1));
                final String units = matcher.group(2);
                final double percentChange = Double.valueOf(matcher.group(3));

                // record the observation and the count of observations
                changeByPeriod[period][0] += percentChange;
                changeByPeriod[period][1] += 1.0d;

                if (logger.isDebugEnabled()) {
                    final double yi = y0 * (1 + percentChange);
                    logger.debug(String.format("Outcome: %s, %s: +%d, change: %f, new value: %f, probability: %f",
                            outcome, units, period, percentChange, yi, probs[i]));
                }
            }

            // build points for predictive data set
            double yn = y0;

            // we need to track the points and remove any that were not
            // predicted by this execution of the model
            final Timestamp endOfPredictionRange = new Timestamp(topOfPeriod.getTime()
                    + (changeByPeriod.length * timeUnits.getDuration()));
            final List<DataSetPoint> existingPoints = this.dataSetPointDao.findByTimeRange(
                    model.getPredictionDataSet(), topOfPeriod, endOfPredictionRange);

            for (int period = 0; period < changeByPeriod.length; period++) {
                final double totalPercentChange = changeByPeriod[period][0];
                final double sampleCount = changeByPeriod[period][1];
                double percentChange;

                if (totalPercentChange == 0.0d || sampleCount == 0.0d) {
                    percentChange = 0.0d;
                } else {
                    percentChange = totalPercentChange / sampleCount;
                }

                // apply adjustments only if the initial point is within the
                // time period, and only for the first time period
                boolean applyAdjustment = period == 0 && topOfPeriod.before(initial.getX());

                if (applyAdjustment) {
                    final double adjustmentFactor = findAdjustmentFactor(initial.getX(), timeUnits);
                    percentChange = (totalPercentChange / sampleCount) * adjustmentFactor;
                }

                // figure out the next value and coerce to a sane number of
                // decimal places (2);
                final double newValue = (double) Math.round(yn * (1.0d + percentChange) * 100) / 100;

                final Timestamp timestamp = new Timestamp(topOfPeriod.getTime() + (period * timeUnits.getDuration()));

                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Model %d for data set %d predicted point: (%s, %f)", model.getId(),
                            model.getTrainingDataSet().getId(), DateFormat.getInstance().format(timestamp), newValue));
                }

                DataSetPoint ithPoint = this.dataSetPointDao.findByTimestamp(model.getPredictionDataSet(), timestamp);

                // conditionally create
                if (ithPoint == null) {
                    ithPoint = new DataSetPoint(model.getPredictionDataSet(), timestamp, newValue);
                    this.dataSetPointDao.add(ithPoint);
                } else {
                    // or update
                    ithPoint.setY(newValue);

                    // updated points retained, other existing removed
                    existingPoints.remove(ithPoint);
                }

                // store current and use as starting point for next iteration
                yn = newValue;
            }

            // remove stale points from an old model execution
            for (final DataSetPoint toRemove : existingPoints) {
                this.dataSetPointDao.remove(toRemove);
            }

            result = true;

        } catch (final Exception e) {
            logger.warn("Model " + model.getId() + " failed with: " + e, e);
            result = false;
        } finally {
            stopWatch.stop();
            logger.info(String.format("Executing model %d took %s", model.getId(), stopWatch.toString()));
        }

        return result;
    }

    /**
     * Returns 1 - the percentage of time period completed. This applies the
     * percent change predicted uniformly over the time period
     * 
     */
    private double findAdjustmentFactor(final Date date, final TimeUnits timeUnits) {
        final Date floor = TimeUnits.floor(date, timeUnits);

        final double dividend = (double) (date.getTime() - floor.getTime());
        final double divisor = (double) timeUnits.getDuration();
        final double percentTimeComplete = dividend / divisor;

        return 1.0d - percentTimeComplete;
    }

    private double[] eval(final Model model, final Timestamp topOfPeriod, final Timestamp endOfPeriod,
            final MaxentModel maxentModel) {

        final ScrollableResults scrollableResults = this.documentDao.scrollableSearch(model.getUserId(), null, model
                .getTopic().getSearchExpression(), SortOrder.DATE_ASC, topOfPeriod, endOfPeriod);

        // initialize with an estimated size to prevent a lot of resizing
        final Map<String, Integer> contextMap = new LinkedHashMap<String, Integer>(64 * 1024);

        try {
            while (scrollableResults.next()) {
                final Object[] row = scrollableResults.get();
                final Document document = (Document) row[0];

                if (document == null) {
                    // caused by stale index
                    continue;
                } else {
                    OpenNLPMaxentContextBuilder.append(contextMap, document);
                }
            }
        } finally {
            scrollableResults.close();
        }

        final String[] context = new String[contextMap.size()];
        final float[] values = new float[contextMap.size()];

        // copy map to arrays
        OpenNLPMaxentContextBuilder.buildContextArrays(contextMap, context, values);

        // eval
        return maxentModel.eval(context, values);
    }

}
