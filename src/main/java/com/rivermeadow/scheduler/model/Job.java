package com.rivermeadow.scheduler.model;

import java.io.Serializable;
import java.util.UUID;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.joda.time.DateTime;

/**
 * Job interface
 */
public interface Job extends Serializable {
    enum Status {
        /**
         * The job failed.
         */
        ERROR,
        /**
         * The job is the database waiting to be queued for execution.
         */
        PENDING,
        /**
         * The job is in the execution queue and waiting.
         */
        QUEUED,
        /**
         * The job is currently running.
         */
        RUNNING,
        /**
         * The job successfully ran.
         */
        SUCCESS;

        public static Status parse(String status) {
            return Status.valueOf(status.toUpperCase());
        }
    }

    public static final String NOW = "now";

    /**
     * Returns the id.
     *
     * @return id
     */
    UUID getId();

    /**
     * Returns a job's sechdule time as a {@link DateTime}.
     *
     * @return schedule time
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


    /**
     * Gets the job's current status.
     *
     * @return job's status
     */
    Status getStatus();

    /**
     * Sets the job's status.
     *
     * @param status status
     */
    void setStatus(Status status);
}
