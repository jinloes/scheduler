package com.rivermeadow.api.service;

import com.rivermeadow.api.model.Job;

/**
 * Service for interacting with {@link Job}s.
 */
public interface JobService {
    /**
     * Adds a job to the schedule queue.
     *
     * @param job job
     */
    void queueJob(Job job);

    /**
     * Returns a job for the given job id.
     *
     * @param jobId job id
     *
     * @return job if one exists
     */
    Job getJob(String jobId);
}
