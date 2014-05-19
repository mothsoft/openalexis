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

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.domain.DataSetAggregationAction;
import com.mothsoft.alexis.domain.DataSetPoint;
import com.mothsoft.alexis.domain.TimeUnits;

@Repository
public class DataSetPointDaoImpl implements DataSetPointDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void add(DataSetPoint point) {
        this.em.persist(point);
    }

    @Override
    public void update(DataSetPoint point) {
        this.em.merge(point);
    }

    @Override
    public DataSetPoint findByTimestamp(DataSet dataSet, Timestamp timestamp) {
        final Query query = this.em
                .createQuery("SELECT p FROM DataSetPoint p WHERE p.dataSet.id = :dataSetId AND p.x = :timestamp");
        query.setParameter("dataSetId", dataSet.getId());
        query.setParameter("timestamp", timestamp);

        @SuppressWarnings("unchecked")
        final List<DataSetPoint> points = query.getResultList();

        if (points.isEmpty()) {
            return null;
        } else {
            return points.get(0);
        }
    }

    @Override
    public List<DataSetPoint> findAndAggregatePointsGroupedByUnit(DataSet dataSet, Timestamp startDate,
            Timestamp endDate, TimeUnits granularity) {
        String pattern = "";

        switch (granularity) {
            case MONTH:
                pattern = "%Y-%m";
                break;
            case DAY:
                pattern = "%Y-%m-%d";
                break;
            case HOUR:
                pattern = "%Y-%m-%d %H:00:00";
                break;
            case MINUTE:
                pattern = "%Y-%m-%d %H:%i:00";
                break;
        }

        final String dateFormat = String.format(" DATE_FORMAT(pt.x, '%s') ", pattern);
        final String dateFormat2 = String.format(" DATE_FORMAT(x, '%s') ", pattern);

        final DataSetAggregationAction action = dataSet.getType().getAggregationAction();

        Query query;

        final String queryString;
        switch (action) {
            case LAST:
                queryString = String
                        .format("select %s, pt.y from data_set_point pt "
                                + " inner join "
                                + "      (select max(id) pt2_id from data_set_point "
                                + "       where data_set_id = :dataSetId and data_set_point.x >= :startDate and data_set_point.x < :endDate GROUP BY %s) pt2 "
                                + " on pt.id = pt2_id", dateFormat, dateFormat2);
                query = this.em.createNativeQuery(queryString);

                break;
            default:
                queryString = String.format("SELECT %s, %s(pt.y) FROM DataSetPoint pt "
                        + "WHERE pt.dataSet.id = :dataSetId AND pt.x >= :startDate AND pt.x < :endDate GROUP BY %s",
                        dateFormat, action.name(), dateFormat);
                query = this.em.createQuery(queryString);
        }

        query.setParameter("dataSetId", dataSet.getId());
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        final List<?> objects = query.getResultList();
        final List<DataSetPoint> points = new ArrayList<DataSetPoint>(objects.size());

        final String sdfPattern;
        switch (granularity) {
            case MONTH:
                sdfPattern = "yyyy-MM";
                break;
            case DAY:
                sdfPattern = "yyyy-MM-dd";
                break;
            case HOUR:
            case MINUTE:
                sdfPattern = "yyyy-MM-dd HH:mm:ss";
                break;
            default:
                throw new IllegalStateException();
        }

        final SimpleDateFormat formatter = new SimpleDateFormat(sdfPattern);

        for (final Object object : objects) {
            final Object[] ith = (Object[]) object;
            final String x = (String) ith[0];
            final Double y = (Double) ith[1];
            try {
                points.add(new DataSetPoint(formatter.parse(x), y));
            } catch (ParseException e) {
                throw new IllegalStateException("This shouldn't happen", e);
            }
        }

        return points;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataSetPoint findLastPointBefore(final DataSet dataSet, final Timestamp timestamp) {
        final String queryString = "SELECT p FROM DataSetPoint p WHERE p.dataSet = :dataSet AND p.x <= :timestamp ORDER BY p.x DESC";
        final Query query = this.em.createQuery(queryString);
        query.setParameter("dataSet", dataSet);
        query.setParameter("timestamp", timestamp);
        query.setMaxResults(1);

        final List<DataSetPoint> points = query.getResultList();

        if (points.isEmpty()) {
            return null;
        } else {
            return points.get(0);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataSetPoint> findByTimeRange(DataSet dataSet, Timestamp startDate, Timestamp endDate) {
        final String queryString = "SELECT p FROM DataSetPoint p WHERE p.dataSet = :dataSet AND p.x >= :startDate AND p.x <= :endDate ORDER BY p.x ASC";
        final Query query = this.em.createQuery(queryString);
        query.setParameter("dataSet", dataSet);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        final List<DataSetPoint> points = query.getResultList();
        return points;
    }

    @Override
    public void remove(DataSetPoint point) {
        this.em.remove(point);
    }

}
