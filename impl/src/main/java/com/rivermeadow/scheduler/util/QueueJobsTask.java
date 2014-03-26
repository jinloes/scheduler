package com.rivermeadow.scheduler.util;

import com.rivermeadow.api.service.JobService;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Task that will periodically ping Cassandra for jobs ready to run and queue them up in Zookeeper.
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
        logger.debug("Checking for jobs ready to run.");
        jobService.queueJobs();
    }
}
