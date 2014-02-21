package com.rivermeadow.api.dao;

import com.rivermeadow.api.model.Job;

/**
 * Provides data access for a {@link Job}.
 */
public interface JobDAO {

    /**
     * Checks a job's existence. A {@link org.springframework.dao.DataRetrievalFailureException} will be thrown
     * if the job does not exist.
     *
     * @param id id
     */
    void checkExists(String id) throws Exception;

    /**
     * Schedules a job to be run.
     *
     * @param job job
     */
    void queueJob(Job job);

    /**
     * Saves a job into the archive.
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
    Job getJob(String id) throws Exception;

    /**
     * Updates the job. Old job data will be overwritten.
     *
     * @param job job
     */
    void updateJob(Job job);
}
