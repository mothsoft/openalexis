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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.junit.Before;
import org.junit.Test;

import com.mothsoft.alexis.dao.DataSetPointDao;
import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.domain.DataSetPoint;
import com.mothsoft.alexis.domain.DateConstants;
import com.mothsoft.alexis.domain.TimeUnits;

public class CorrelationCalculatorTest {

    private DataSetPointDao dao;
    private CorrelationCalculator calculator;

    private Timestamp now;
    private Timestamp yesterday;
    private Timestamp yesterdayPlus2Hours;
    private Timestamp yesterdayPlus3Hours;
    private Timestamp yesterdayPlus4Hours;
    private Timestamp yesterdayPlus5Hours;
    private DataSet ds1;
    private DataSet ds2;

    @Before
    public void setUp() {
        this.dao = mock(DataSetPointDao.class);
        this.calculator = new CorrelationCalculatorImpl(this.dao);

        final Long millis = System.currentTimeMillis();
        this.now = new Timestamp(millis);

        this.yesterday = new Timestamp(millis - DateConstants.ONE_DAY_IN_MILLISECONDS);
        this.yesterdayPlus2Hours = new Timestamp(millis + (2 * 60 * 60 * 1000));
        this.yesterdayPlus3Hours = new Timestamp(millis + (3 * 60 * 60 * 1000));
        this.yesterdayPlus4Hours = new Timestamp(millis + (4 * 60 * 60 * 1000));
        this.yesterdayPlus5Hours = new Timestamp(millis + (5 * 60 * 60 * 1000));

        this.ds1 = mock(DataSet.class);
        when(this.ds1.getId()).thenReturn(1L);

        this.ds2 = mock(DataSet.class);
        when(this.ds2.getId()).thenReturn(2L);
    }

    @Test
    public void testCalculatorAgainstCommonsMath() {
        assertEquals(Long.valueOf(1), ds1.getId());
        assertEquals(Long.valueOf(2), ds2.getId());

        final List<DataSetPoint> ds1Points = new ArrayList<DataSetPoint>();
        ds1Points.add(new DataSetPoint(yesterdayPlus2Hours, 3.14));
        ds1Points.add(new DataSetPoint(yesterdayPlus3Hours, 1.0));
        ds1Points.add(new DataSetPoint(yesterdayPlus4Hours, 0.0));
        ds1Points.add(new DataSetPoint(yesterdayPlus5Hours, 7.8));

        when(this.dao.findAndAggregatePointsGroupedByUnit(ds1, this.yesterday, this.now, TimeUnits.HOUR))
                .thenReturn(ds1Points);

        final List<DataSetPoint> ds2Points = new ArrayList<DataSetPoint>();
        ds2Points.add(new DataSetPoint(yesterdayPlus2Hours, 3.14));
        ds2Points.add(new DataSetPoint(yesterdayPlus3Hours, 1.0));
        ds2Points.add(new DataSetPoint(yesterdayPlus4Hours, 2.5));
        ds2Points.add(new DataSetPoint(yesterdayPlus5Hours, 0.0));

        when(this.dao.findAndAggregatePointsGroupedByUnit(ds2, this.yesterday, this.now, TimeUnits.HOUR))
                .thenReturn(ds2Points);
        final PearsonsCorrelation correlation = new PearsonsCorrelation();

        final double[] x = { 3.14, 1.0, 0.0, 7.8 };
        final double[] y = { 3.14, 1.0, 2.5, 0.0 };

        final double r = correlation.correlation(x, y);
        assertEquals(-0.6048295, r, 0.001);

        final double r2 = this.calculator.correlate(this.ds1, this.ds2, this.yesterday, this.now,
                TimeUnits.HOUR);
        assertEquals(r, r2, 0.001);
    }

    @Test
    public void testCalculatorSimpleR1() {
        final List<DataSetPoint> ds1Points = new ArrayList<DataSetPoint>();
        ds1Points.add(new DataSetPoint(yesterdayPlus2Hours, 3.0));
        ds1Points.add(new DataSetPoint(yesterdayPlus3Hours, 4.0));
        ds1Points.add(new DataSetPoint(yesterdayPlus4Hours, 5.5));

        when(this.dao.findAndAggregatePointsGroupedByUnit(ds1, this.yesterday, this.now, TimeUnits.HOUR))
                .thenReturn(ds1Points);

        final List<DataSetPoint> ds2Points = new ArrayList<DataSetPoint>();
        ds2Points.add(new DataSetPoint(yesterdayPlus2Hours, 3.0));
        ds2Points.add(new DataSetPoint(yesterdayPlus3Hours, 4.0));
        ds2Points.add(new DataSetPoint(yesterdayPlus4Hours, 5.5));

        when(this.dao.findAndAggregatePointsGroupedByUnit(ds2, this.yesterday, this.now, TimeUnits.HOUR))
                .thenReturn(ds2Points);

        assertEquals(Double.valueOf(1.0),
                (Double) this.calculator.correlate(ds1, ds2, yesterday, now, TimeUnits.HOUR));
    }

    @Test
    public void testCommonsMathCorrelationIdentity() {
        final PearsonsCorrelation correlation = new PearsonsCorrelation();

        final double[] x = { 0.0, 1.0, 2.3, 3.4 };
        final double[] y = { 0.0, 1.0, 2.3, 3.4 };

        final double r = correlation.correlation(x, y);
        assertEquals(Double.valueOf(1.0), Double.valueOf(r));
    }

    @Test
    public void testCommonsMathCorrelationIdentityMatrix() {
        final PearsonsCorrelation correlation = new PearsonsCorrelation();

        final double[][] xy = new double[4][2];
        xy[0][0] = 0.0;
        xy[1][0] = 1.0;
        xy[2][0] = 2.3;
        xy[3][0] = 3.4;

        xy[0][1] = 0.0;
        xy[1][1] = 1.0;
        xy[2][1] = 2.3;
        xy[3][1] = 3.4;

        final RealMatrix matrix = correlation.computeCorrelationMatrix(xy);
        assertEquals(2, matrix.getColumnDimension());
        assertEquals(2, matrix.getRowDimension());
        assertEquals(1.0, matrix.getEntry(0, 1), 0.000001);
        assertEquals(1.0, matrix.getEntry(1, 0), 0.000001);
    }
}
