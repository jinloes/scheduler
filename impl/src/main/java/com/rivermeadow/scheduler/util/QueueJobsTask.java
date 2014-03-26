package com.rivermeadow.scheduler.util;

import com.rivermeadow.api.service.JobService;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by jinloes on 3/24/14.
 */
@Component
public class QueueJobsTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(QueueJobsTask.class);
    private final JobService jobService;

    @Autowired
    public QueueJobsTask(JobService jobService) {
        this.jobService = jobService;
    }

    @Override
    public void run() {
        logger.debug(new DateTime() + " I'm leader!");
        jobService.queueJobs();
    }
}
