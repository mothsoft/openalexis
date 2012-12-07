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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.internal.logging.Logger;

import com.mothsoft.alexis.domain.Calculator;
import com.mothsoft.alexis.domain.DataSetPoint;

public abstract class AbstractModelTrainer implements ModelTrainer {

    private static final Logger logger = Logger.getLogger(AbstractModelTrainer.class);

    /**
     * Calculate the percent change in the list of points
     * 
     * @param points
     *            - points
     * @param pointMap
     *            - map of points to date
     * @return map of Date to Float percent change (1.0 = 100%)
     */
    public Map<Date, Float> calculatePercentChange(final List<DataSetPoint> points,
            final Map<Date, DataSetPoint> pointMap) {

        final Map<Date, Float> changeMap = new HashMap<Date, Float>();

        // need at least two points to calculate...
        if (points.isEmpty() || points.size() == 1) {
            return changeMap;
        }

        // find max to use for scaling (0->n) and (n->0) transitions
        final double yMax = findMax(points);

        // calculate percent change against the prior point for each
        // point.
        DataSetPoint pn = points.get(0);

        for (int i = 1; i < points.size(); i++) {
            final double yn = pn.getY();

            final DataSetPoint pi = points.get(i);
            final double yi = pi.getY();
            final double percentChange = Calculator.calculatePercentChange(yn, yi, yMax);
            changeMap.put(pi.getX(), (float) percentChange);

            if (logger.isDebugEnabled()) {
                final String message = String.format("Point (%s, %f), percent change given by yn=%f, yi=%f = %f",
                        pi.getX(), pi.getY(), yn, yi, percentChange);
                logger.debug(message);
            }

            // save this one for comparison next time
            pn = pi;
        }

        return changeMap;
    }

    /**
     * Find the maximum value (y) in a set of points
     * 
     * @param points
     * @return - maximum y value in the list of points
     */
    public double findMax(final List<DataSetPoint> points) {
        double max = 0.0;

        for (final DataSetPoint point : points) {
            double y = point.getY();

            if (y > max) {
                max = y;
            }
        }

        return max;
    }

    /**
     * Store each point in a map by date
     * 
     * @param points
     *            list of points
     * 
     * @return map containing all the points keyed by date
     */
    public Map<Date, DataSetPoint> toMap(final List<DataSetPoint> points) {
        final Map<Date, DataSetPoint> map = new HashMap<Date, DataSetPoint>();

        for (final DataSetPoint point : points) {
            map.put(point.getX(), point);
        }

        return map;
    }

}
