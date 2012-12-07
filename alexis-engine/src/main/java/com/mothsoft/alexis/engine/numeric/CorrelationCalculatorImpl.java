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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.mothsoft.alexis.dao.DataSetPointDao;
import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.domain.DataSetPoint;
import com.mothsoft.alexis.domain.TimeUnits;

/**
 * Rough algorithm: Get all points for ds1 and ds2 in time range, grouped by
 * granularity and summed. Leave 0 as placeholder in any periods when ds1 has
 * data and ds2 does not (or vice versa)
 * 
 */
public class CorrelationCalculatorImpl implements CorrelationCalculator {

    private DataSetPointDao dao;

    public CorrelationCalculatorImpl(DataSetPointDao dao) {
        super();
        this.dao = dao;
    }

    @Override
    public double[][] correlate(List<DataSet> dataSets, Timestamp startDate, Timestamp endDate, TimeUnits granularity) {
        if (dataSets == null || dataSets.size() < 2) {
            throw new IllegalArgumentException("At least 2 data sets are required for correlation");
        }

        final Map<Long, List<Double>> orderedPoints = new TreeMap<Long, List<Double>>();

        for (int i = 0; i < dataSets.size(); i++) {
            final DataSet dataSet = dataSets.get(i);
            final List<DataSetPoint> points = this.dao.findAndAggregatePointsGroupedByUnit(dataSet, startDate, endDate,
                    granularity);
            for (final DataSetPoint point : points) {
                final Long millis = point.getX().getTime();
                if (!orderedPoints.containsKey(millis)) {
                    orderedPoints.put(millis, newDoubleList(dataSets.size()));
                }
                orderedPoints.get(millis).set(i, point.getY());
            }
        }

        if (orderedPoints.size() <= 1) {
            throw new IllegalArgumentException("Needed at least 2 points, found: " + orderedPoints.size());
        }

        final double[][] points = new double[orderedPoints.size()][dataSets.size()];

        int i = 0;
        for (final Map.Entry<Long, List<Double>> entry : orderedPoints.entrySet()) {
            final List<Double> values = entry.getValue();
            for (int j = 0; j < values.size(); j++) {
                points[i][j] = values.get(j);
            }
            i++;
        }

        final PearsonsCorrelation correlation = new PearsonsCorrelation();
        final RealMatrix matrix = correlation.computeCorrelationMatrix(points);
        return matrix.getData();
    }

    private static List<Double> newDoubleList(int size) {
        final Double zeroDotZero = Double.valueOf(0.0d);
        final List<Double> list = new ArrayList<Double>(size);
        for (int i = 0; i < size; i++) {
            list.add(zeroDotZero);
        }
        return list;
    }

    @Override
    public double correlate(DataSet ds1, DataSet ds2, Timestamp startDate, Timestamp endDate, TimeUnits granularity) {
        final List<DataSet> dataSets = new ArrayList<DataSet>(2);
        dataSets.add(ds1);
        dataSets.add(ds2);

        final double[][] correlationMatrix = this.correlate(dataSets, startDate, endDate, granularity);
        return correlationMatrix[0][1];
    }

}
