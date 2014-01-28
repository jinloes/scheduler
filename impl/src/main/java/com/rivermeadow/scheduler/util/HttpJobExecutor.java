package com.rivermeadow.scheduler.util;

import java.util.Map;

import com.rivermeadow.api.dao.JobDAO;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.util.JobExecutor;

import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * {@link JobExecutor} that handles http scheme.
 */
@Component("http")
public class HttpJobExecutor implements JobExecutor {
    private final RestTemplate restTemplate;
    private final JobDAO jobDAO;

    @Autowired
    public HttpJobExecutor(final RestTemplate restTemplate, final JobDAO jobDAO) {
        this.restTemplate = restTemplate;
        this.jobDAO = jobDAO;
    }

    @Override
    public void execute(Job job) {
        //TODO(jinloes) can probably move the status updates to an abstract class
        // Job is about to run, so update status
        job.setStatus(Job.Status.RUNNING);
        jobDAO.updateJob(job);
        try {
            //TODO(jinloes) add other methods
            switch(job.getTask().getMethod().toLowerCase()) {
                case "post":
                        ResponseEntity<Map> response = restTemplate.postForEntity(job.getTask().getUri(),
                                job.getTask().getBody(), Map.class);
                        int statusCode = response.getStatusCode().value();
                        Range<Integer> expectedRange = job.getTask().getExpectedRange();
                        if(!job.getTask().getExpectedRange().contains(statusCode)) {
                            throw new RuntimeException(String.format("Response code: %s was not in expected range. ",
                                    statusCode, expectedRange.toString()));
                        };
                    break;
            }
        } catch (RestClientException e) {
            //TODO(jinloes) possibly log exception
            // Return gracefully because we don't want a job with an http error to be requeued
            job.setStatus(Job.Status.ERROR);
            jobDAO.updateJob(job);
        }
        // Job completed successfully update and return
        job.setStatus(Job.Status.SUCCESS);
        jobDAO.updateJob(job);
    }
}
