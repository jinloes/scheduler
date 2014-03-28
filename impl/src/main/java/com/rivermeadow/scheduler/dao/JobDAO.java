package com.rivermeadow.scheduler.dao;

import java.util.Date;
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
    void save(Job job);

    /**
     * Returns a job for the given id.
     *
     * @param id id
     * @return job for the given id, otherwise null
     */
    Job getById(UUID id);

    /**
     * Return jobs before the provided date and equal to the status.
     *
     * @param status job status
     * @param date   date upper bound
     *
     * @return list of jobs, empty list if none are found
     */
    List<Job> getJobsBeforeDate(Job.Status status, Date date, int limit);

    /**
     * Updates the job. Old job data will be overwritten.
     *
     * @param job job
     */
    void updateJob(Job job);

    /**
     * Updates a job's status.
     *
     * @param jobId  job id
     * @param status status
     */
    void updateStatus(UUID jobId, Job.Status status);
}
