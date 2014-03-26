package com.rivermeadow.scheduler.dao;

import java.util.List;
import java.util.UUID;

import com.rivermeadow.api.model.Job;

/**
 * Provides data access for a {@link Job}.
 */
public interface JobDAO {
    /**
     * Saves a job.
     *
     * @param job job
     */
    void saveJob(Job job);

    /**
     * Returns a job for the given id.
     *
     * @param id id
     *
     * @return job
     */
    Job getJob(String id);

    /**
     * Return jobs before the current date that are pending to be run.
     *
     * @return list of jobs
     */
    List<Job> getJobsBeforeNow();

    /**
     * Updates the job. Old job data will be overwritten.
     *
     * @param job job
     */
    void updateJob(Job job);

    /**
     * Updates a job's status.
     *
     * @param jobId job id
     * @param status status
     */
    void updateStatus(UUID jobId, Job.Status status);
}
