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

import java.util.List;

import com.mothsoft.alexis.domain.Topic;
import com.mothsoft.alexis.domain.TopicDocument;

public interface TopicDao {

    public void add(Topic topic);
    
    public void add(TopicDocument topicDocument);

    public Topic findTopicByUserAndName(Long userId, String name);

    public Topic get(Long id);

    /**
     * FIXME - won't be long until this is completely insufficient...
     * 
     * @return
     */
    public List<Topic> list();

    public List<Topic> listTopicsByOwner(Long userId);

    public void remove(Topic topic);

    public void update(Topic topic);

}
