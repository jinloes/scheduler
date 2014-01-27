package com.rivermeadow.scheduler.util;

import java.util.Map;

import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.util.JobExecutor;

import org.apache.commons.lang3.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * {@link JobExecutor} that handles http scheme.
 */
@Component("http")
public class HttpJobExecutor implements JobExecutor {
    @Override
    public void execute(Job job) {
        System.out.println("Running job.");
        RestTemplate restTemplate = new RestTemplate();
        switch(job.getTask().getMethod()) {
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
    }
}
