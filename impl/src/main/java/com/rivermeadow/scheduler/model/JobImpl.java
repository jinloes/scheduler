package com.rivermeadow.scheduler.model;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.model.Task;
import com.rivermeadow.api.validation.Schedule;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
    public JobImpl(@JsonProperty("task") final TaskImpl task,
            @JsonProperty("schedule") final String schedule) {
        this(UUID.randomUUID(), task, schedule, Status.PENDING);
    }

    public JobImpl(UUID id, TaskImpl task, String schedule, Status status) {
        this.id = id;
        this.task = task;
        this.schedule = schedule;
        this.status = status;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public DateTime getSchedule() {
        if (scheduleDate != null) {
            return scheduleDate;
        }
        // Lazily initialize the schedule date
        if (NOW.equals(schedule)) {
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
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("task", task)
                .append("schedule", schedule)
                .append("status", status)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(task, schedule, status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final JobImpl other = (JobImpl) obj;
        return Objects.equals(this.task, other.task) &&
                Objects.equals(this.schedule, other.schedule) &&
                Objects.equals(this.status, other.status);
    }
}
