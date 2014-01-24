package com.rivermeadow.scheduler.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Objects;
import com.rivermeadow.api.model.Task;

/**
 */
public class TaskImpl implements Task {
    private final Map<String, Object> task;

    @JsonCreator
    public TaskImpl(final Map<String, Object> task) {
        this.task = task;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("task", task)
                .toString();
    }
}
