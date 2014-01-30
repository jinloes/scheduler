package com.rivermeadow.api.web;

import com.rivermeadow.api.model.Job;

import org.springframework.http.ResponseEntity;

/**
 * Controller for scheduling jobs.
 */
public interface JobController<T extends Job>{
    static final String ROOT_JOB_PATH = "/jobs";
    static final String JOB_LINK = "%s" + ROOT_JOB_PATH + "/%s";
    /**
     * Queues up a job.
     *
     * @param job job
     *
     * @return value
     */
    ResponseEntity scheduleJob(T job);

    /**
     * Returns a job for a given job id.
     *
     * @param jobId job id
     *
     * @return job
     */
    Job getJob(String jobId);
}
