package com.rivermeadow.scheduler.util;

import com.rivermeadow.scheduler.model.Job;

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
