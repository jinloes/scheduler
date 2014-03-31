package com.rivermeadow.scheduler.util;

import com.rivermeadow.scheduler.model.Job;

/**
 * A listener that is called during a job execution lifecycle.
 */
public interface JobListener {
    /**
     * Called before a job is executed.
     *
     * @param job job
     */
    void beforeJobExecution(Job job);

    /**
     * Called after a job has successfully completed.
     *
     * @param job job
     */
    void onJobSuccess(Job job);

    /**
     * Called after a job has failed.
     *
     * @param e exception
     * @param job job
     */
    void onJobFailure(Exception e, Job job);
}
