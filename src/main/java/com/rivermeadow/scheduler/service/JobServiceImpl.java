package com.rivermeadow.scheduler.service;

import java.util.List;
import java.util.UUID;

import com.rivermeadow.scheduler.model.Job;
import com.rivermeadow.scheduler.dao.JobDAO;
import com.rivermeadow.scheduler.model.JobQueue;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link JobService}
 */
@Service
public class JobServiceImpl implements JobService {
    private static final int JOB_LIMIT = 500;
    private final JobDAO jobDAO;
    private final JobQueue jobQueue;

    @Autowired
    public JobServiceImpl(JobDAO jobDAO, JobQueue jobQueue) {
        this.jobDAO = jobDAO;
        this.jobQueue = jobQueue;
    }

    @Override
    public void save(Job job) {
        jobDAO.save(job);
    }

    @Override
    public void queueJobs() {
        List<Job> jobsToExecute;
        while (!(jobsToExecute = jobDAO.getJobsBeforeDate(Job.Status.PENDING,
                DateTime.now().toDate(), JOB_LIMIT)).isEmpty()) {
            for (Job job : jobsToExecute) {
                jobQueue.queueJob(job);
                // Mark the job as added to the job queue
                // Could be useful if we find a lot of jobs have been queued but not executed for
                // whatever reason
                jobDAO.updateStatus(job.getId(), Job.Status.QUEUED);
            }
        }
    }

    @Override
    public Job getById(UUID jobId) {
        return jobDAO.getById(jobId);
    }
}
