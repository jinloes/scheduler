package com.rivermeadow.scheduler.service;

import com.rivermeadow.api.dao.JobDAO;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.service.JobService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link com.rivermeadow.api.service.JobService}
 */
@Service
public class JobServiceImpl implements JobService {
    private final JobDAO jobDAO;

    @Autowired
    public JobServiceImpl(final JobDAO jobDAO) {
        this.jobDAO = jobDAO;
    }

    @Override
    public void queueJob(Job job) {
        jobDAO.queueJob(job);
        jobDAO.saveJob(job);
    }

    @Override
    public Job getJob(String jobId) {
        jobDAO.checkExists(jobId);
        return jobDAO.getJob(jobId);
    }
}
