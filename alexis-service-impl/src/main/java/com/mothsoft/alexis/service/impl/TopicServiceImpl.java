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

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.dao.DataSetDao;
import com.mothsoft.alexis.dao.TopicDao;
import com.mothsoft.alexis.domain.Topic;
import com.mothsoft.alexis.domain.TopicActivityDataSet;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.TopicService;

@Transactional
public class TopicServiceImpl implements TopicService {

    private DataSetDao dataSetDao;
    private TopicDao topicDao;

    public void setDataSetDao(final DataSetDao dataSetDao) {
        this.dataSetDao = dataSetDao;
    }

    public void setTopicDao(final TopicDao topicDao) {
        this.topicDao = topicDao;
    }

    public void add(final Topic topic) {
        this.topicDao.add(topic);
    }

    public Topic findTopicByUserAndName(Long userId, String name) {
        CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(userId);
        return this.topicDao.findTopicByUserAndName(userId, name);
    }

    public Topic get(final Long id) {
        final Topic topic = this.topicDao.get(id);
        CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(topic.getUserId());
        return topic;
    }

    public List<Topic> listTopicsByOwner(final Long userId) {
        CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(userId);
        return this.topicDao.listTopicsByOwner(userId);
    }

    public void remove(final Long id) {
        final Topic topic = this.topicDao.get(id);
        CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(topic.getUserId());

        final TopicActivityDataSet dataSet = this.dataSetDao.findTopicActivityDataSet(id);

        if (dataSet != null) {
            this.dataSetDao.remove(dataSet);
        }

        this.topicDao.remove(topic);
    }

    @Override
    public void update(Long id, String name, String searchExpression, String description) {
        final Topic topic = this.topicDao.get(id);
        CurrentUserUtil.assertAuthenticatedUserOrAdminOrSystem(topic.getUserId());
        topic.setName(name);
        topic.setSearchExpression(searchExpression);
        topic.setDescription(description);
        this.topicDao.update(topic);
    }

}
