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
import java.util.List;

import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.domain.DataSetType;
import com.mothsoft.alexis.domain.TopicActivityDataSet;

public interface DataSetDao {

    void add(DataSet set);

    DataSet findAggregateTopicActivityDataSet(Long userId);

    List<TopicActivityDataSet> findMostActiveTopicDataSets(Long userId, Timestamp startDate, Timestamp endDate,
            int limit);

    DataSet findSystemDataSet(DataSetType type, String name);

    List<TopicActivityDataSet> findTopicActivityDataSetsByUser(Long userId);

    TopicActivityDataSet findTopicActivityDataSet(Long topicId);

    DataSet get(Long id);

    List<DataSet> list(Long userId);

    void remove(DataSet set);

    void update(DataSet set);

}
