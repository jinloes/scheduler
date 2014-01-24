package com.rivermeadow.scheduler.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.model.Task;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Model for storing a request to queue a job.
 */
public class JobImpl implements Job {
    private final Task task;
    private final String schedule;
    private final DateTime scheduleDate;

    @JsonCreator
    private JobImpl(@JsonProperty("task") final TaskImpl task, @JsonProperty("schedule") final String schedule) {
        this.task = task;
        this.schedule = schedule;
        if(NOW.equals(schedule)) {
            scheduleDate = new DateTime();
        } else {
            scheduleDate = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(schedule);
        }
    }

    @Override
    public String getSchedule() {
        return schedule;
    }

    @Override
    public DateTime getScheduleDate() {
        return scheduleDate;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("task", task)
                .add("schedule", schedule)
                .toString();
    }
}
