package com.rivermeadow.scheduler.service;

import java.util.Date;

import com.google.common.collect.Lists;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.scheduler.dao.JobDAO;
import com.rivermeadow.scheduler.model.JobImpl;
import com.rivermeadow.scheduler.model.JobQueue;
import com.rivermeadow.scheduler.web.CustomMatchers;

import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.testng.annotations.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;

import static org.hamcrest.Matchers.lessThanOrEqualTo;

/**
 * Tests for {@link JobServiceImpl}.
 */
public class JobServiceTest {
    @Injectable
    private JobDAO jobDAO;

    @Injectable
    private JobQueue jobQueue;

    @Tested
    private JobServiceImpl jobService;

    @Test
    public void testQueueJobs() {
        final Job job = new JobImpl(null, null);
        new Expectations() {{
            jobDAO.getJobsBeforeDate(Job.Status.PENDING,
                    (Date) with(CustomMatchers.isBeforeOrEqualToDate(DateTime.now().plusMinutes(1)
                            .toDate())),
                    500);
            returns(Lists.newArrayList(job));
            jobQueue.queueJob(job);
            jobDAO.updateStatus(job.getId(), Job.Status.QUEUED);
            jobDAO.getJobsBeforeDate(Job.Status.PENDING,
                    (Date) with(CustomMatchers.isBeforeOrEqualToDate(DateTime.now().plusMinutes(1)
                            .toDate())),
                    500);
            returns(Lists.newArrayList());
        }};
        jobService.queueJobs();
    }
}
