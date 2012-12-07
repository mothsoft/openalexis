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
package com.mothsoft.alexis.web;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;

import org.apache.log4j.Logger;

import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.domain.DateConstants;
import com.mothsoft.alexis.domain.TimeUnits;
import com.mothsoft.alexis.service.DataSetService;

public class CorrelationBackingBean {

    private static final Logger logger = Logger.getLogger(CorrelationBackingBean.class);

    private DataSetService dataSetService;
    private SelectSeriesBackingBean selectSeriesBackingBean;

    private double[][] correlationMatrix;
    private List<String> dataSetLabels;

    public CorrelationBackingBean() {
    }

    public DataSetService getDataSetService() {
        return dataSetService;
    }

    public void setDataSetService(DataSetService dataSetService) {
        this.dataSetService = dataSetService;
    }

    public SelectSeriesBackingBean getSelectSeriesBackingBean() {
        return selectSeriesBackingBean;
    }

    public void setSelectSeriesBackingBean(SelectSeriesBackingBean selectSeriesBackingBean) {
        this.selectSeriesBackingBean = selectSeriesBackingBean;
    }

    public void correlate(final AjaxBehaviorEvent event) {
        try {
            this.dataSetLabels = new ArrayList<String>();

            final Timestamp startDate = new Timestamp(this.getSelectSeriesBackingBean().getStartDate().getTime());
            final Timestamp endDate = new Timestamp(this.getSelectSeriesBackingBean().getEndDate().getTime());

            final List<DataSet> dataSets = new ArrayList<DataSet>(this.selectSeriesBackingBean.getSelectedSeries()
                    .size());

            for (final String label : this.selectSeriesBackingBean.getSelectedSeries()) {
                final Long dataSetId = Long.valueOf(label);
                final DataSet dataSet = this.dataSetService.get(dataSetId);
                dataSets.add(dataSet);
                this.dataSetLabels.add(dataSet.getName());
            }

            final Long duration = endDate.getTime() - startDate.getTime();
            final TimeUnits units;

            if (duration > DateConstants.ONE_WEEK_IN_MILLISECONDS) {
                units = TimeUnits.DAY;
            } else {
                units = TimeUnits.HOUR;
            }

            this.correlationMatrix = this.dataSetService.correlate(dataSets, startDate, endDate, units);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e, e);
            }
            final FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getLocalizedMessage(),
                    e.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage(event.getComponent().getClientId(), message);
        }
    }

    public double[][] getCorrelationMatrix() {
        return this.correlationMatrix;
    }

    public List<String> getDataSetLabels() {
        return this.dataSetLabels;
    }
}
