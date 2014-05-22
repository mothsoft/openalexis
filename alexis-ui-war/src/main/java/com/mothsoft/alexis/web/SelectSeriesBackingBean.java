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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.faces.model.SelectItem;

import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.DataSetService;

public class SelectSeriesBackingBean {

    private DataSetService dataSetService;

    private List<SelectItem> selectItems;
    private List<String> selectedSeries;
    private Date startDate;
    private Date endDate;

    public SelectSeriesBackingBean() {
        super();

        final TimeZone timeZone = CurrentUserUtil.getTimeZone();

        final GregorianCalendar calendar = new GregorianCalendar(timeZone);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        this.endDate = calendar.getTime();

        // default start to beginning of same day
        calendar.add(Calendar.DATE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        this.startDate = calendar.getTime();
    }

    public DataSetService getDataSetService() {
        return dataSetService;
    }

    public void setDataSetService(DataSetService dataSetService) {
        this.dataSetService = dataSetService;
    }

    public List<SelectItem> getSelectItems() {
        if (this.selectItems == null) {
            final Long userId = CurrentUserUtil.getCurrentUserId();
            this.selectItems = new ArrayList<SelectItem>();
            final List<DataSet> dataSets = this.dataSetService.listDataSets(userId);

            if (!dataSets.isEmpty()) {
                String dataSetType = dataSets.get(0).getType().getName();
                this.selectItems.add(new SelectItem("", "--" + dataSetType + "--", "", true));

                for (final DataSet dataSet : dataSets) {
                    if (!dataSet.getType().getName().equals(dataSetType)) {
                        dataSetType = dataSet.getType().getName();
                        final SelectItem selectItem = new SelectItem(dataSetType + ".sectionHeader", "--" + dataSetType
                                + "--", "", true);
                        this.selectItems.add(selectItem);
                    }

                    final String value = "" + dataSet.getId();
                    this.selectItems.add(new SelectItem(value, " " + dataSet.getName()));
                }
            }
        }

        return this.selectItems;
    }

    public void setSelectItems(List<SelectItem> selectItems) {
        this.selectItems = selectItems;
    }

    public List<String> getSelectedSeries() {
        return selectedSeries;
    }

    public void setSelectedSeries(List<String> selectedSeries) {
        this.selectedSeries = selectedSeries;
    }

    public String getSelectedSingleSeries() {
        if (selectedSeries == null || selectedSeries.isEmpty()) {
            return null;
        }
        return selectedSeries.get(0);
    }

    public void setSelectedSingleSeries(String selectedSingleSeries) {
        this.selectedSeries = new ArrayList<String>(1);
        this.selectedSeries.add(selectedSingleSeries);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}