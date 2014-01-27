package com.rivermeadow.api.util;

import com.rivermeadow.api.model.Job;

/**
 * Interface for executing jobs.
 */
public interface JobExecutor {
    public void execute(Job job);
}
