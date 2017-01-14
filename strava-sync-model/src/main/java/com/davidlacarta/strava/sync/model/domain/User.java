package com.davidlacarta.strava.sync.model.domain;

import com.davidlacarta.strava.sync.model.BaseModel;

import org.hibernate.annotations.GenericGenerator;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The persistent class for the user database table.
 */
@Entity
@Table(name = "USER")
public class User extends BaseModel {

    @Id
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @GeneratedValue(generator = "uuid")
    @Column(name = "USER_ID")
    private String id;

    @Column(name = "USER_ID_STRAVA")
    private String idStrava;

    @Column(name = "USER_CODE_STRAVA")
    private String codeStrava;

    @Column(name = "USER_ID_COACH")
    private String idCoach;

    @Column(name = "USER_PASSWORD_COACH")
    private String passwordCoach;

    @Column(name = "USER_SYNC")
    private Boolean sync;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
    private Set<Activity> activities;

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdStrava() {
        return idStrava;
    }

    public void setIdStrava(String idStrava) {
        this.idStrava = idStrava;
    }

    public String getIdCoach() {
        return idCoach;
    }

    public void setIdCoach(String idCoach) {
        this.idCoach = idCoach;
    }

    public String getPasswordCoach() {
        return passwordCoach;
    }

    public void setPasswordCoach(String passwordCoach) {
        this.passwordCoach = passwordCoach;
    }

    public Set<Activity> getActivities() {
        return activities;
    }

    public void setActivities(Set<Activity> activities) {
        this.activities = activities;
    }

    public String getCodeStrava() {
        return codeStrava;
    }

    public void setCodeStrava(String codeStrava) {
        this.codeStrava = codeStrava;
    }

    public Boolean getSync() {
        return sync;
    }

    public void setSync(Boolean sync) {
        this.sync = sync;
    }
}
