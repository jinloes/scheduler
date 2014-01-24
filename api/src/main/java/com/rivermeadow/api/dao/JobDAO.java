package com.rivermeadow.api.dao;

import com.rivermeadow.api.model.Job;

/**
 * Provides data access for a {@link .Job}.
 */
public interface JobDAO {
    /**
     * Schedules a job to be run.
     *
     * @param job job
     */
    void addJob(Job job);
}
