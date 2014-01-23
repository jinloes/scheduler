package com.rivermeadow.api.model;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.rivermeadow.api.validation.Schedule;

/**
 * Job interface
 */
public interface Job extends Serializable {
    public static final String NOW = "now";
    /**
     * Returns the schedule value for the job. The value is either 'now' or an ISO8601 format date string.
     *
     * @return schedule value
     */
    @Schedule
    String getSchedule();
}
