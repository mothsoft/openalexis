package com.mothsoft.alexis.domain;

import java.util.Date;

public class TopicRef {

    private Long id;
    private Float score;
    private Date creationDate;

    public TopicRef(final Long id, final Float score, final Date creationDate) {
        this.id = id;
        this.score = score;
        this.creationDate = creationDate;
    }

    protected TopicRef() {
        // needed for frameworks
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

}
