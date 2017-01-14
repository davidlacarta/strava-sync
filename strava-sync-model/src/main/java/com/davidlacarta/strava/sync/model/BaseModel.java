package com.davidlacarta.strava.sync.model;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * Base Model Class
 *
 * Every Model Class should extend this class
 */
public abstract class BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {

        return new ReflectionToStringBuilder(this, ToStringStyle.JSON_STYLE).toString();
    }
}
