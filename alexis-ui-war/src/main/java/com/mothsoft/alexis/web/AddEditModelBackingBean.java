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
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import com.mothsoft.alexis.domain.DataSet;
import com.mothsoft.alexis.domain.DataSetType;
import com.mothsoft.alexis.domain.Model;
import com.mothsoft.alexis.domain.ModelType;
import com.mothsoft.alexis.domain.Topic;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.DataSetService;
import com.mothsoft.alexis.service.ModelService;
import com.mothsoft.alexis.service.TopicService;

public class AddEditModelBackingBean {

    private static final String MODEL_PREDICTIONS = "Model Predictions";

    // dependencies
    private DataSetService dataSetService;
    private ModelService modelService;
    private TopicService topicService;

    // state
    private List<SelectItem> trainingDataSetOptions;
    private List<SelectItem> topicOptions;

    private Model model;
    private String name;
    private Long topicId;
    private Long trainingDataSetId;
    private Date startDate;
    private Date endDate;

    public AddEditModelBackingBean() {
        super();

        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, -7);
        this.startDate = calendar.getTime();

        this.endDate = new Date();
    }

    public void setDataSetService(final DataSetService dataSetService) {
        this.dataSetService = dataSetService;
    }

    public void setModelService(final ModelService modelService) {
        this.modelService = modelService;
    }

    public void setTopicService(final TopicService topicService) {
        this.topicService = topicService;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SelectItem> getTrainingDataSetOptions() {
        if (this.trainingDataSetOptions == null) {
            this.trainingDataSetOptions = new ArrayList<SelectItem>();
            final List<DataSet> dataSets = this.dataSetService.listDataSets(CurrentUserUtil.getCurrentUserId());

            if (!dataSets.isEmpty()) {
                String dataSetType = dataSets.get(0).getType().getName();
                this.trainingDataSetOptions.add(new SelectItem("", "--" + dataSetType + "--", "", true));

                for (final DataSet dataSet : dataSets) {
                    if (!dataSet.getType().getName().equals(dataSetType)) {
                        dataSetType = dataSet.getType().getName();
                        final SelectItem selectItem = new SelectItem(dataSetType + ".sectionHeader", "--" + dataSetType
                                + "--", "", true);
                        this.trainingDataSetOptions.add(selectItem);
                    }

                    this.trainingDataSetOptions.add(new SelectItem(dataSet.getId(), " " + dataSet.getName()));
                }
            }
        }
        return this.trainingDataSetOptions;
    }

    public List<SelectItem> getTopicOptions() {
        if (this.topicOptions == null) {
            this.topicOptions = new ArrayList<SelectItem>();
            final List<Topic> topics = this.topicService.listTopicsByOwner(CurrentUserUtil.getCurrentUserId());

            this.topicOptions.add(new SelectItem(null, "--Select--", "", true));

            if (!topics.isEmpty()) {
                for (final Topic ith : topics) {
                    this.topicOptions.add(new SelectItem(ith.getId(), ith.getName()));
                }
            }
        }
        return this.topicOptions;
    }

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public Long getTrainingDataSetId() {
        return trainingDataSetId;
    }

    public void setTrainingDataSetId(Long trainingDataSetId) {
        this.trainingDataSetId = trainingDataSetId;
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

    public boolean isEdit() {
        return this.model != null && this.model.getId() != null;
    }

    public void remove(final ActionEvent event) {
        final Long id = (Long) event.getComponent().getAttributes().get("modelId");
        final Model modelToRemove = this.modelService.get(id);
        this.modelService.remove(modelToRemove);
    }

    public void save(final ActionEvent event) {
        final ModelType type = ModelType.MAXENT; // FIXME
        final DataSetType predictionDataSetType = this.dataSetService.findDataSetType(MODEL_PREDICTIONS);
        final DataSet trainingDataSet = this.dataSetService.get(this.trainingDataSetId);
        final Topic topic = this.topicService.get(this.topicId);
        final Model model = new Model(name, trainingDataSet, topic, type, startDate, endDate, predictionDataSetType);
        this.modelService.add(model);
    }

    public void validateModelName(FacesContext context, UIComponent validate, Object value) {
        final String name = (String) value;
        final Long userId = CurrentUserUtil.getCurrentUserId();

        final Model existingModel = this.modelService.findModelByUserAndName(userId, name);

        if (existingModel != null && existingModel != this.model) {
            ((UIInput) validate).setValid(false);
            final String messageBundle = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
            final String stringMessage = ResourceBundle.getBundle(messageBundle).getString(
                    "validator.modelNameNotUnique");

            final FacesMessage facesMessage = new FacesMessage(stringMessage);
            facesMessage.setSeverity(FacesMessage.SEVERITY_WARN);
            FacesContext.getCurrentInstance().addMessage(validate.getClientId(), facesMessage);
        }
    }

}
