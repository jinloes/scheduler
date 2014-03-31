package com.rivermeadow.scheduler.service;

import java.util.UUID;

import com.rivermeadow.scheduler.model.Job;

/**
 * Service for interacting with {@link Job}s.
 */
public interface JobService {
    /**
     * Adds a job to the schedule queue.
     *
     * @param job job
     */
    void save(Job job);

    /**
     * Finds jobs that are in the pending state and have an execution date before now.
     */
    void queueJobs();

    /**
     * Returns a job for the given job id.
     *
     * @param jobId job id
     *
     * @return job if one exists
     */
    Job getById(UUID jobId);
}
