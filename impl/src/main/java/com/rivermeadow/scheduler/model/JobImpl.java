package com.rivermeadow.scheduler.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.model.Task;
import com.rivermeadow.api.validation.Schedule;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Model for storing a request to queue a job.
 */
public class JobImpl implements Job {
    private static final long serialVersionUID = 8762539992424354755L;

    private final UUID id;
    private final Task task;
    @Schedule
    private final String schedule;
    private DateTime scheduleDate;
    private Status status;

    @JsonCreator
    private JobImpl(@JsonProperty("task") final TaskImpl task,
                    @JsonProperty("schedule") final String schedule) {
        this.id = UUID.randomUUID();
        this.task = task;
        this.schedule = schedule;
        this.status = Status.PENDING;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public DateTime getSchedule() {
        if(scheduleDate != null) {
            return scheduleDate;
        }
        // Lazily initialize the schedule date
        if(NOW.equals(schedule)) {
            scheduleDate = new DateTime();
        } else {
            scheduleDate = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(schedule);
        }
        return scheduleDate;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public String getUriScheme() {
        try {
            return new URI(task.getUri()).getScheme();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to get URI scheme.");
        }
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("task", task)
                .add("schedule", schedule)
                .toString();
    }
}
