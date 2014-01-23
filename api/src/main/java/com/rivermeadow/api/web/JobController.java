package com.rivermeadow.api.web;

import com.rivermeadow.api.model.Job;

import org.springframework.http.ResponseEntity;

/**
 * Controller for scheduling jobs.
 */
public interface JobController<T extends Job>{
    /**
     * Queues up a job.
     *
     * @param job job
     *
     * @return value
     */
    ResponseEntity scheduleJob(T job);
}
