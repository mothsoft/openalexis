/*   Copyright 2015 Tim Garrett, Mothsoft LLC
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
package com.mothsoft.alexis.rest.topic.v1.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.mothsoft.alexis.domain.TopicActivityDataSet;
import com.mothsoft.alexis.rest.topic.v1.Topic;
import com.mothsoft.alexis.rest.topic.v1.TopicResource;
import com.mothsoft.alexis.security.CurrentUserUtil;
import com.mothsoft.alexis.service.DataSetService;

@Transactional
public class TopicResourceImpl implements TopicResource {

	private static final Logger logger = Logger.getLogger(TopicResourceImpl.class);

	private DataSetService service;

	public TopicResourceImpl(final DataSetService service) {
		this.service = service;
	}

	@Override
	public List<Topic> getTopTopics(Timestamp startDate, Timestamp endDate, int count) {
		List<TopicActivityDataSet> dataSets = this.service
		        .findMostActiveTopicDataSets(CurrentUserUtil.getCurrentUserId(), startDate, endDate, count);
		List<Topic> topics = new ArrayList<Topic>(dataSets.size());
		for (final TopicActivityDataSet dataSet : dataSets) {
			topics.add(toDto(dataSet));
		}
		return topics;
	}

	private Topic toDto(TopicActivityDataSet dataSet) {
		final Topic result = new Topic();
		result.setId(dataSet.getTopic().getId());
		result.setName(dataSet.getTopic().getName());
		result.setCount(dataSet.getPointSum().longValue());
		return result;
	}

}
