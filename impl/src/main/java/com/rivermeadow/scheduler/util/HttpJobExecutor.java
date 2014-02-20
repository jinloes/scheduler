package com.rivermeadow.scheduler.util;

import java.util.List;
import java.util.Map;

import com.rivermeadow.api.dao.JobDAO;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.model.ResponseCodeRange;
import com.rivermeadow.api.util.AbstractJobExecutor;
import com.rivermeadow.api.util.JobExecutor;
import com.rivermeadow.api.util.JobListener;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * {@link JobExecutor} that handles http scheme.
 */
@Component("http")
public class HttpJobExecutor extends AbstractJobExecutor {
    private final RestTemplate restTemplate;

    @Autowired
    public HttpJobExecutor(final RestTemplate restTemplate, final JobDAO jobDAO) {
        super(jobDAO, new HttpJobListener(jobDAO));
        this.restTemplate = restTemplate;
    }

    @Override
    protected void executeJob(Job job) {
        String methodStr = job.getTask().getMethod().toLowerCase();
        //TODO(jinloes) should we support GET?
        HttpMethod method = HttpMethod.valueOf(methodStr.toUpperCase());
        ResponseEntity<Map> response = restTemplate.exchange(job.getTask().getUri(),
                method, new HttpEntity<>(job.getTask().getBody()), Map.class);
        int statusCode = response.getStatusCode().value();
        checkExpectedRange(job.getTask().getResponseCodeRanges(), statusCode);
    }

    private void checkExpectedRange(List<ResponseCodeRange> expectedResponseCodes, int statusCode) {
        if (CollectionUtils.isNotEmpty(expectedResponseCodes)) {
            for (ResponseCodeRange range : expectedResponseCodes) {
                if (range.contains(statusCode)) {
                    // The response code was in one of the ranges, so return
                    return;
                }
            }
        } else {
            // The list was empty so accept any value.
            return;
        }
        //TODO(jinloes) use messages.properties
        throw new RuntimeException(String.format("Response code: %s was not in expected range. ",
                statusCode, expectedResponseCodes.toString()));
    }

    /**
     * A listener for executing an http job.
     */
    private static class HttpJobListener extends DefaultJobListener {
        private JobDAO jobDAO;

        public HttpJobListener(final JobDAO jobDAO) {
            this.jobDAO = jobDAO;
        }

        @Override
        public void onJobFailure(Exception e, Job job) {
            if (e instanceof RestClientException) {
                // Return gracefully because we don't want a job with an http error to be requeued
                job.setStatus(Job.Status.ERROR);
                jobDAO.updateJob(job);
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                // Wrap the exception
                //TODO(jinloes) see if there's a better way to wrap this exception
                throw new RuntimeException(e);
            }
        }
    }
}
