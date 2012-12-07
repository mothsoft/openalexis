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
package com.mothsoft.alexis.rest.dataset.v1.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.domain.DateConstants;
import com.mothsoft.alexis.domain.TimeUnits;
import com.mothsoft.alexis.rest.dataset.v1.Correlation;
import com.mothsoft.alexis.rest.dataset.v1.DataSet;
import com.mothsoft.alexis.rest.dataset.v1.DataSetPoint;
import com.mothsoft.alexis.rest.dataset.v1.DataSetResource;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.DataSetService;

@Transactional
public class DataSetResourceImpl implements DataSetResource {

    private static final Logger logger = Logger.getLogger(DataSetResourceImpl.class);

    private DataSetService service;

    public DataSetResourceImpl(final DataSetService service) {
        this.service = service;
    }

    @Override
    public List<DataSetPoint> findAndAggregatePointsGroupedByUnit(Long dataSetId, Timestamp startDate,
            Timestamp endDate, String units) {
        final TimeUnits unitEnum;

        if (units == null) {
            final long duration = endDate.getTime() - startDate.getTime();
            if (duration >= DateConstants.ONE_WEEK_IN_MILLISECONDS) {
                unitEnum = TimeUnits.DAY;
            } else {
                unitEnum = TimeUnits.HOUR;
            }
        } else {
            unitEnum = TimeUnits.valueOf(units);
        }

        final List<com.mothsoft.alexis.domain.DataSetPoint> pointDomains = this.service
                .findAndAggregatePointsGroupedByUnit(dataSetId, startDate, endDate, unitEnum);
        return toDto(pointDomains);
    }

    private List<DataSetPoint> toDto(List<com.mothsoft.alexis.domain.DataSetPoint> points) {
        final List<DataSetPoint> dtos = new ArrayList<DataSetPoint>(points.size());

        for (final com.mothsoft.alexis.domain.DataSetPoint point : points) {
            dtos.add(new DataSetPoint(point.getX(), point.getY()));
        }

        return dtos;
    }

    @Override
    public DataSet getDataSet(Long id) {
        return toDto(service.get(id));
    }

    @Override
    public List<DataSet> list() {
        final List<com.mothsoft.alexis.domain.DataSet> domains = this.service.listDataSets(CurrentUserUtil
                .getCurrentUserId());

        final List<DataSet> dtos = new ArrayList<DataSet>(domains.size());

        for (final com.mothsoft.alexis.domain.DataSet domain : domains) {
            dtos.add(toDto(domain));
        }

        return dtos;
    }

    @Override
    public Correlation correlate(Long dataSetAId, Long dataSetBId, Timestamp startDate, Timestamp endDate, String units) {
        if (units == null) {
            final Response response = Response.status(Status.BAD_REQUEST)
                    .entity("Invalid Request: parameter 'units' expected").build();
            throw new WebApplicationException(response);
        }
        final TimeUnits unitEnum = TimeUnits.valueOf(units);

        com.mothsoft.alexis.domain.DataSet ds1 = null;
        com.mothsoft.alexis.domain.DataSet ds2 = null;

        try {
            ds1 = this.service.get(dataSetAId);
            ds2 = this.service.get(dataSetBId);
        } catch (final EntityNotFoundException nfe) {
            final Response response = Response.status(Status.BAD_REQUEST).entity("Invalid Request: unknown dataset")
                    .build();
            throw new WebApplicationException(response);
        }

        if (startDate == null) {
            final Calendar calendar = new GregorianCalendar(1900, 0, 1);
            startDate = new Timestamp(calendar.getTime().getTime());
            logger.debug("Start Date: " + startDate.toLocaleString());
        }

        if (endDate == null) {
            endDate = new Timestamp(System.currentTimeMillis());
            logger.debug("End Date: " + endDate.toLocaleString());
        }

        return new Correlation(this.service.correlate(ds1, ds2, startDate, endDate, unitEnum));
    }

    public com.mothsoft.alexis.rest.dataset.v1.DataSet toDto(final com.mothsoft.alexis.domain.DataSet domain) {
        final com.mothsoft.alexis.rest.dataset.v1.DataSet dto = new com.mothsoft.alexis.rest.dataset.v1.DataSet();

        dto.setId(domain.getId());
        dto.setName(domain.getName());
        dto.setUserId(domain.getUserId());
        dto.setType(domain.getType().getName());

        if (domain.getParentDataSet() != null) {
            dto.setParentDataSetId(domain.getParentDataSet().getId());
            dto.setParentDataSetName(domain.getParentDataSet().getName());
        }

        return dto;
    }

}
