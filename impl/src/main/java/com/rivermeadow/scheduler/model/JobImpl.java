package com.rivermeadow.scheduler.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Objects;
import com.jayway.jsonpath.JsonPath;
import com.rivermeadow.api.model.Job;

/**
 * Model for storing a request to queue a job.
 */
public class JobImpl implements Job {
    private static final JsonPath SCHEDULE_PATH = JsonPath.compile("$.body.schedule");
    private final Map<String, Object> request;

    @JsonCreator
    private JobImpl(final Map<String, Object> request) {
        this.request = request;
    }

    @Override
    public String getSchedule() {
        return SCHEDULE_PATH.read(request);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .addValue(request)
                .toString();
    }
}
