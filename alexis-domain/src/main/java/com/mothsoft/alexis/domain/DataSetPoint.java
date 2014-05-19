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

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity(name = "DataSetPoint")
@Table(name = "data_set_point")
public class DataSetPoint {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "x")
    private Timestamp x;

    @Column(name = "y")
    private Double y;

    @ManyToOne
    @JoinColumn(name = "data_set_id")
    private DataSet dataSet;

    @Version
    @Column(name = "version", columnDefinition = "smallint unsigned")
    protected Integer version;

    public DataSetPoint(final Date x, final Double y) {
        this.x = new Timestamp(x.getTime());
        this.y = y;
    }

    public DataSetPoint(final Timestamp x, final Double y) {
        this.x = x;
        this.y = y;
    }

    public DataSetPoint(final DataSet dataSet, final Date x, final Double y) {
        this.dataSet = dataSet;
        this.x = new Timestamp(x.getTime());
        this.y = y;
    }

    protected DataSetPoint() {
        super();
    }

    public Long getId() {
        return id;
    }

    public Timestamp getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public DataSet getDataSet() {
        return dataSet;
    }

}
