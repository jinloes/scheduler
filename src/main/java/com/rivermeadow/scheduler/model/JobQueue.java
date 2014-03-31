package com.rivermeadow.scheduler.model;

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
