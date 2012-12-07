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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity(name = "DataSet")
@Table(name = "data_set")
@Inheritance(strategy = InheritanceType.JOINED)
public class DataSet {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", length = 64)
    private String name;

    @ManyToOne
    @JoinColumn(name = "data_set_type_id")
    private DataSetType type;

    @ManyToOne
    @JoinColumn(name = "parent_data_set_id")
    private DataSet parentDataSet;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    @JoinColumn(name = "data_set_id")
    @OrderBy("x ASC")
    private List<DataSetPoint> points;

    @Column(name = "is_aggregate", columnDefinition = "bit")
    private boolean aggregate;

    /**
     * Construct a shared data set (no user)
     */
    public DataSet(String name, DataSetType type) {
        this.name = name;
        this.type = type;
        this.points = new ArrayList<DataSetPoint>();
    }

    /**
     * Construct
     */
    public DataSet(final Long userId, final String name, final DataSetType type, final boolean isAggregate) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.aggregate = isAggregate;
    }

    public DataSet(final Long userId, final String name, final DataSetType type, final DataSet parentDataSet,
            final boolean isAggregate) {
        this(userId, name, type, isAggregate);
        this.parentDataSet = parentDataSet;
    }

    protected DataSet() {
        super();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public DataSet getParentDataSet() {
        return this.parentDataSet;
    }

    public DataSetType getType() {
        return type;
    }

    public List<DataSetPoint> getPoints() {
        return points;
    }

    public boolean isAggregate() {
        return this.aggregate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataSet other = (DataSet) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        return true;
    }

}
