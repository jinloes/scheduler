package com.rivermeadow.api.util;

import com.rivermeadow.api.model.Job;

/**
 * Interface for executing jobs.
 */
public interface JobExecutor {
    /**
     * Executes a job.
     *
     * @param job job
     */
    void execute(Job job);
}
