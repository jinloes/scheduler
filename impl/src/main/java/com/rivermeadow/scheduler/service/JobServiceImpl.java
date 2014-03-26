package com.rivermeadow.scheduler.service;

import java.util.List;

import com.rivermeadow.scheduler.dao.JobDAO;
import com.rivermeadow.api.exception.MessageArgumentException;
import com.rivermeadow.api.exception.NotFoundException;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.service.JobService;
import com.rivermeadow.api.util.ErrorCodes;
import com.rivermeadow.scheduler.model.JobQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link JobService}
 */
@Service
public class JobServiceImpl implements JobService {
    private final JobDAO jobDAO;
    private final JobQueue jobQueue;

    @Autowired
    public JobServiceImpl(JobDAO jobDAO, JobQueue jobQueue) {
        this.jobDAO = jobDAO;
        this.jobQueue = jobQueue;
    }

    @Override
    public void saveJob(Job job) {
        jobDAO.saveJob(job);
    }

    @Override
    public void queueJobs() {
        List<Job> jobsToExecute = jobDAO.getJobsBeforeNow();
        for(Job job: jobsToExecute) {
            jobQueue.queueJob(job);
            // Mark the job as added to the job queue
            // Could be useful if we find a lot of jobs have been queued but not executed for
            // whatever reason
            jobDAO.updateStatus(job.getId(), Job.Status.QUEUED);
        }
    }

    @Override
    public Job getJob(String jobId) {
        return jobDAO.getJob(jobId);
    }
}
