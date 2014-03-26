package com.rivermeadow.scheduler.model;

import com.rivermeadow.api.model.Job;

/**
 * Queue for jobs ready to be executed.
 */
public interface JobQueue {
    /**
     * Adds a job to the queue to be executed.
     *
     * @param job job
     */
    void queueJob(Job job);
}
