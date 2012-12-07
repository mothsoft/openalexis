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
package com.mothsoft.alexis.service.impl;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.dao.DataSetDao;
import com.mothsoft.alexis.dao.DataSetPointDao;
import com.mothsoft.alexis.dao.DataSetTypeDao;
import com.mothsoft.alexis.dao.TopicDao;
import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.domain.DataSetPoint;
import com.mothsoft.alexis.domain.DataSetType;
import com.mothsoft.alexis.domain.TimeUnits;
import com.mothsoft.alexis.domain.Topic;
import com.mothsoft.alexis.domain.TopicActivityDataSet;
import com.mothsoft.alexis.engine.numeric.CorrelationCalculator;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.DataSetService;

@Transactional
public class DataSetServiceImpl implements DataSetService {

    private DataSetDao dataSetDao;
    private DataSetTypeDao dataSetTypeDao;
    private DataSetPointDao dataSetPointDao;
    private TopicDao topicDao;
    private CorrelationCalculator correlationCalculator;

    public DataSetServiceImpl(final DataSetDao dataSetDao, final DataSetTypeDao dataSetTypeDao,
            final DataSetPointDao dataSetPointDao, final TopicDao topicDao,
            final CorrelationCalculator correlationCalculator) {
        this.dataSetDao = dataSetDao;
        this.dataSetTypeDao = dataSetTypeDao;
        this.dataSetPointDao = dataSetPointDao;
        this.topicDao = topicDao;
        this.correlationCalculator = correlationCalculator;
    }

    @Override
    public void addDataSet(DataSet set) {
        this.dataSetDao.add(set);
    }

    @Override
    public void addDataSetType(DataSetType type) {
        this.dataSetTypeDao.add(type);
    }

    @Override
    public double[][] correlate(List<DataSet> dataSets, Timestamp startDate, Timestamp endDate, TimeUnits granularity) {
        for (final DataSet dataSet : dataSets) {
            if (dataSet.getUserId() != null) {
                CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(dataSet.getUserId());
            }
        }
        return this.correlationCalculator.correlate(dataSets, startDate, endDate, granularity);
    }

    @Override
    public double correlate(DataSet ds1, DataSet ds2, Timestamp startDate, Timestamp endDate, TimeUnits granularity) {
        if (ds1.getUserId() != null) {
            CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(ds1.getUserId());
        }
        if (ds2.getUserId() != null) {
            CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(ds2.getUserId());
        }
        return this.correlationCalculator.correlate(ds1, ds2, startDate, endDate, granularity);
    }

    @Override
    public DataSetType findDataSetType(String name) {
        return this.dataSetTypeDao.findSystemDataSetType(name);
    }

    @Override
    public DataSet get(Long id) {
        final DataSet dataSet = this.dataSetDao.get(id);

        if (dataSet == null) {
            throw new EntityNotFoundException("DataSet:" + id + " not found.");
        }

        if (dataSet.getUserId() != null) {
            CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(dataSet.getUserId());
        }

        return dataSet;
    }

    @Override
    public List<DataSet> listDataSets(Long userId) {
        CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(userId);
        return this.dataSetDao.list(userId);
    }

    @Override
    public void updateDataSet(DataSet set) {
        CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(set.getUserId());
        this.dataSetDao.update(set);
    }

    @Override
    public DataSet findAggregateTopicActivityDataSet(Long userId) {
        CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(userId);
        return this.dataSetDao.findAggregateTopicActivityDataSet(userId);
    }

    @Override
    public List<DataSetPoint> findAndAggregatePointsGroupedByUnit(Long dataSetId, Timestamp startDate,
            Timestamp endDate, TimeUnits granularity) {
        final DataSet dataSet = this.get(dataSetId);

        if (dataSet.getUserId() != null) {
            CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(dataSet.getUserId());
        }

        return this.dataSetPointDao.findAndAggregatePointsGroupedByUnit(dataSet, startDate, endDate, granularity);
    }

    @Override
    public List<TopicActivityDataSet> findMostActiveTopicDataSets(Long userId, Timestamp startDate, Timestamp endDate,
            int limit) {
        CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(userId);
        return this.dataSetDao.findMostActiveTopicDataSets(userId, startDate, endDate, limit);
    }

    @Override
    public TopicActivityDataSet findTopicActivityDataSet(Long topicId) {
        final Topic topic = this.topicDao.get(topicId);
        CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(topic.getUserId());
        return this.dataSetDao.findTopicActivityDataSet(topicId);
    }

    @Override
    public List<TopicActivityDataSet> findTopicActivityDataSets(Long userId) {
        CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(userId);
        return this.dataSetDao.findTopicActivityDataSetsByUser(userId);
    }

}
