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

import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import com.mothsoft.alexis.domain.Topic;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.TopicService;

public class AddEditTopicBackingBean {

    private TopicService topicService;

    private Long id;
    private Topic topic;

    private String name;
    private String searchExpression;
    private String description;

    public AddEditTopicBackingBean() {
        this.topic = new Topic();
        this.searchExpression = "";
    }

    public void setTopicService(TopicService topicService) {
        this.topicService = topicService;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearchExpression() {
        return this.searchExpression;
    }

    public void setSearchExpression(String searchExpression) {
        this.searchExpression = searchExpression;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEdit() {
        return this.topic != null && this.topic.getId() != null;
    }

    public String load() {
        this.topic = this.topicService.get(this.id);
        this.name = this.topic.getName();
        this.searchExpression = this.topic.getSearchExpression();
        this.description = this.topic.getDescription();

        return null;
    }

    public void remove(final ActionEvent event) {
        final Long id = (Long) event.getComponent().getAttributes().get("topicId");
        this.topicService.remove(id);
    }

    public void save(final ActionEvent event) {

        topic.setName(this.getName());
        topic.setSearchExpression(this.searchExpression);
        topic.setDescription(this.getDescription());

        if (isEdit()) {
            this.topicService.update(topic.getId(), topic.getName(), topic.getSearchExpression(),
                    topic.getDescription());
        } else {
            topic.setUserId(CurrentUserUtil.getCurrentUserId());
            this.topicService.add(topic);
        }
    }

    public void validateTopicName(FacesContext context, UIComponent validate, Object value) {
        final String name = (String) value;
        final Long userId = CurrentUserUtil.getCurrentUserId();

        final Topic existingTopic = this.topicService.findTopicByUserAndName(userId, name);

        if (existingTopic != null && existingTopic.getId() != this.topic.getId()) {
            ((UIInput) validate).setValid(false);
            final String messageBundle = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
            final String stringMessage = ResourceBundle.getBundle(messageBundle).getString(
                    "validator.topicNameNotUnique");

            final FacesMessage facesMessage = new FacesMessage(stringMessage);
            facesMessage.setSeverity(FacesMessage.SEVERITY_WARN);
            FacesContext.getCurrentInstance().addMessage(validate.getClientId(), facesMessage);
        }
    }
}
