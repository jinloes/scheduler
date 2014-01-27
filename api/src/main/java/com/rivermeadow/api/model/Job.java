package com.rivermeadow.api.model;

import java.io.Serializable;

import com.rivermeadow.api.validation.Schedule;

import org.joda.time.DateTime;

/**
 * Job interface
 */
public interface Job extends Serializable {
    public static final String NOW = "now";

    /**
     * Returns job's schedule time. The value is either 'now' or an ISO8601 format date string.
     *
     * @return schedule value
     */
    @Schedule
    String getSchedule();

    /**
     * Returns a job's sechdule time as a {@link DateTime}.
     * @return
     */
    DateTime getScheduleDate();

    /**
     * Returns job's task.
     *
     * @return task
     */
    Task getTask();

    /**
     * Returns a job's uri scheme.
     *
     * @return uri scheme.
     */
    String getUriScheme();
}
