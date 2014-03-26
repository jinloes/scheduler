package com.rivermeadow.api.model;

import java.io.Serializable;
import java.util.UUID;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rivermeadow.api.validation.Schedule;

import org.joda.time.DateTime;

/**
 * Job interface
 */
public interface Job extends Serializable {
    enum Status {
        ERROR,
        PENDING,
        QUEUED,
        RUNNING,
        SUCCESS;

        public static Status parse(String status) {
            return Status.valueOf(status.toUpperCase());
        }
    }

    public static final String NOW = "now";

    UUID getId();

    /**
     * Returns a job's sechdule time as a {@link DateTime}.
     * @return
     */
    DateTime getSchedule();

    /**
     * Returns job's task.
     *
     * @return task
     */
    @Valid
    Task getTask();

    /**
     * Returns a job's uri scheme.
     *
     * @return uri scheme.
     */
    @JsonIgnore
    String getUriScheme();

    Status getStatus();

    void setStatus(Status status);
}
