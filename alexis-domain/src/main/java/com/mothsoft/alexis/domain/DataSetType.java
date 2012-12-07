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
package com.mothsoft.alexis.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "DataSetType")
@Table(name = "data_set_type")
public class DataSetType {

    public static final String TOPIC_ACTIVITY = "Topic Activity";

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "is_system", columnDefinition = "bits")
    private boolean system;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", length = 64)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregation_action", columnDefinition = "char(4)")
    private DataSetAggregationAction aggregationAction;

    public DataSetType(boolean system, Long userId, String name, DataSetAggregationAction aggregationAction) {
        this.system = system;
        this.userId = userId;
        this.name = name;
    }

    protected DataSetType() {
        super();
    }

    public Long getId() {
        return id;
    }

    public boolean isSystem() {
        return system;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public DataSetAggregationAction getAggregationAction() {
        return this.aggregationAction;
    }
}
