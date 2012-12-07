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
import com.mothsoft.alexis.domain.DataSetPoint;
import com.mothsoft.alexis.domain.TimeUnits;

public interface DataSetPointDao {

    public void add(final DataSetPoint point);

    public DataSetPoint findLastPointBefore(DataSet dataSet, Timestamp timestamp);

    public DataSetPoint findByTimestamp(DataSet dataSet, Timestamp timestamp);

    public List<DataSetPoint> findByTimeRange(DataSet dataSet, Timestamp startDate, Timestamp endDate);

    public List<DataSetPoint> findAndAggregatePointsGroupedByUnit(DataSet dataSet, Timestamp startDate,
            Timestamp endDate, TimeUnits granularity);

    public void remove(DataSetPoint point);

}
