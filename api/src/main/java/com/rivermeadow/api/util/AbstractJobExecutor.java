package com.rivermeadow.api.util;

import com.rivermeadow.api.dao.JobDAO;
import com.rivermeadow.api.model.Job;

/**
 * Base class for executing jobs.
 */
public abstract class AbstractJobExecutor implements JobExecutor {
    protected final JobDAO jobDAO;
    private final JobListener jobListener;

    public AbstractJobExecutor(final JobDAO jobDAO, final JobListener jobListener) {
        this.jobDAO = jobDAO;
        this.jobListener = jobListener;
    }

    @Override
    public void execute(Job job) {
        beforeJobExecution(job);
        try {
            executeJob(job);
            onJobSuccess(job);
        } catch (Exception e) {
            onJobFailure(e, job);
        }
    }

    private void beforeJobExecution(Job job) {
        // Job is about to run, so update status
        job.setStatus(Job.Status.RUNNING);
        jobDAO.updateJob(job);
        if(jobListener != null) {
            jobListener.beforeJobExecution(job);
        }
    }

    protected abstract void executeJob(Job job);

    private void onJobSuccess(Job job) {
        // Job completed successfully update and return
        job.setStatus(Job.Status.SUCCESS);
        jobDAO.updateJob(job);
        if(jobListener != null) {
            jobListener.onJobSuccess(job);
        }
    }

    private void onJobFailure(Exception e, Job job) {
        if(jobListener != null) {
            jobListener.onJobFailure(e, job);
        }
    }
}
