package com.rivermeadow.scheduler.util;

import com.rivermeadow.scheduler.dao.JobDAO;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.util.JobExecutor;
import com.rivermeadow.api.util.JobListener;

/**
 * Base class for executing jobs.
 */
public abstract class AbstractJobExecutor implements JobExecutor {
    protected final JobDAO jobDAO;
    private final JobListener jobListener;

    public AbstractJobExecutor(final JobDAO jobDAO) {
        this.jobDAO = jobDAO;
        this.jobListener = new DefaultJobListener();
    }

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
        jobDAO.updateStatus(job.getId(), Job.Status.RUNNING);
        jobListener.beforeJobExecution(job);
    }

    /**
     * Executes a job.
     *
     * @param job job
     */
    protected abstract void executeJob(Job job);

    private void onJobSuccess(Job job) {
        // Job completed successfully update and return
        jobDAO.updateStatus(job.getId(), Job.Status.SUCCESS);
        jobListener.onJobSuccess(job);
    }

    private void onJobFailure(Exception e, Job job) {
        jobListener.onJobFailure(e, job);
    }

    /**
     * A job listener that does nothing. This class should be extended and methods overwritten.
     */
    public static class DefaultJobListener implements JobListener {

        @Override
        public void beforeJobExecution(Job job) {
            // Do nothing
        }

        @Override
        public void onJobSuccess(Job job) {
            // Do nothing
        }

        @Override
        public void onJobFailure(Exception e, Job job) {
            // Do nothing
        }
    }
}
