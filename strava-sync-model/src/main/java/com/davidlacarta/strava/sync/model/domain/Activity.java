package com.davidlacarta.strava.sync.model.domain;

import com.davidlacarta.strava.sync.model.BaseModel;

import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The persistent class for the activity database table.
 */
@Entity
@Table(name = "ACTIVITY")
public class Activity extends BaseModel {

    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    @Column(name = "ACTIVITY_ID")
    private String id;

    @Column(name = "ACTIVITY_KEY_COACH")
    private String keyCoach;

    @Column(name = "ACTIVITY_SYNC")
    private Boolean sync;

    @Column(name = "ACTIVITY_SYNC_DATE")
    private LocalDateTime syncDate;

    @ManyToOne
    @JoinColumn(name = "ACTIVITY_USER")
    private User user;

    public Activity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKeyCoach() {
        return keyCoach;
    }

    public void setKeyCoach(String keyCoach) {
        this.keyCoach = keyCoach;
    }

    public Boolean getSync() {
        return sync;
    }

    public void setSync(Boolean sync) {
        this.sync = sync;
    }

    public LocalDateTime getSyncDate() {
        return syncDate;
    }

    public void setSyncDate(LocalDateTime syncDate) {
        this.syncDate = syncDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}

