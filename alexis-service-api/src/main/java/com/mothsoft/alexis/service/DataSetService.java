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
package com.mothsoft.alexis.service;

import java.sql.Timestamp;
import java.util.List;

import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.domain.DataSetPoint;
import com.mothsoft.alexis.domain.DataSetType;
import com.mothsoft.alexis.domain.TimeUnits;
import com.mothsoft.alexis.domain.TopicActivityDataSet;

public interface DataSetService {

    public void addDataSet(DataSet set);

    public void addDataSetType(DataSetType type);

    public double[][] correlate(List<DataSet> dataSets, Timestamp startDate, Timestamp endDate,
            TimeUnits granularity);

    public double correlate(DataSet ds1, DataSet ds2, Timestamp startDate, Timestamp endDate,
            TimeUnits granularity);

    DataSet findAggregateTopicActivityDataSet(Long userId);

    public List<DataSetPoint> findAndAggregatePointsGroupedByUnit(Long dataSetId, Timestamp startDate, Timestamp endDate,
            TimeUnits granularity);

    public DataSetType findDataSetType(String name);

    List<TopicActivityDataSet> findMostActiveTopicDataSets(Long userId, Timestamp startDate, Timestamp endDate,
            int limit);

    TopicActivityDataSet findTopicActivityDataSet(Long topicId);

    List<TopicActivityDataSet> findTopicActivityDataSets(Long userId);

    public DataSet get(Long id);

    public List<DataSet> listDataSets(Long userId);

    public void updateDataSet(DataSet set);

}
