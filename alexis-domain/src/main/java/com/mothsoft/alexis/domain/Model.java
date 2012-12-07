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

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name = "Model")
@Table(name = "model")
public class Model {

    /**
     * To faciliate a fixed number of outcomes, will map outcomes to the
     * greatest number a percentage does not exceed
     */
    public static final double[] OUTCOME_ARRAY = new double[] { 0.0d, 0.000125d, 0.00025d, 0.000375d, 0.0005d,
            0.000625d, 0.00075d, 0.000875, 0.001d, 0.00125d, 0.0025d, 0.00375d, 0.005d, 0.00625d, 0.0075d, 0.00875,
            0.01d, 0.0125d, 0.015d, 0.0175d, 0.02d, 0.0225d, 0.0250d, 0.0275d, 0.03d, 0.04d, 0.05d, 0.075d, 0.1d,
            0.125d, 0.15d, 0.2d, 0.3d, 0.4d, 0.50d, 0.6d, 0.7d, 0.8d, 0.9d, 1.0d, 2.0d, 3.0d, 4.0d, 5.0d, 10.0d,
            100.0d, 1000.0d, Double.POSITIVE_INFINITY };

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "training_data_set_id", updatable = false)
    private DataSet trainingDataSet;

    @OneToOne(cascade = { CascadeType.ALL })
    @JoinColumn(name = "prediction_data_set_id")
    private DataSet predictionDataSet;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "topic_id", updatable = false)
    private Topic topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 10, columnDefinition = "char(10)", updatable = false)
    private ModelType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 10, columnDefinition = "char(10)", nullable = false)
    private ModelState state = ModelState.PENDING;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Column(name = "lookahead", columnDefinition = "INTEGER")
    private int lookahead = 12;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_units", length = 10, columnDefinition = "char(10)", nullable = false)
    private TimeUnits timeUnits = TimeUnits.HOUR;

    public Model() {
        super();
    }

    public Model(final String name, final DataSet trainingDataSet, final Topic topic, final ModelType type,
            final Date startDate, final Date endDate, final DataSetType predictionDataSetType) {
        this.name = name;
        this.trainingDataSet = trainingDataSet;
        this.topic = topic;
        this.userId = topic.getUserId();
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;

        this.predictionDataSet = new DataSet(this.userId, name + " (model)", predictionDataSetType,
                this.trainingDataSet, false);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return this.userId;
    }

    public DataSet getPredictionDataSet() {
        return this.predictionDataSet;
    }

    public DataSet getTrainingDataSet() {
        return this.trainingDataSet;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
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

    public int getLookahead() {
        return lookahead;
    }

    public void setLookahead(int lookahead) {
        this.lookahead = lookahead;
    }

    public TimeUnits getTimeUnits() {
        return timeUnits;
    }

    public void setTimeUnits(TimeUnits timeUnits) {
        this.timeUnits = timeUnits;
    }

    public ModelType getType() {
        return type;
    }

    public ModelState getState() {
        return this.state;
    }

    public void setState(ModelState state) {
        this.state = state;
    }

    public void onTrainingComplete() {
        if (getState() != ModelState.TRAINING) {
            throw new IllegalStateException("Model " + getId() + " in state " + getState().name()
                    + " was not marked as training!");
        }

        this.state = ModelState.READY;
    }

}
