package com.rivermeadow.scheduler.util;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.rivermeadow.api.dao.JobDAO;
import com.rivermeadow.api.exception.MessageArgumentException;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.model.ResponseCodeRange;
import com.rivermeadow.scheduler.model.JobImpl;
import com.rivermeadow.scheduler.model.TaskImpl;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;

/**
 * Tests for {@link com.rivermeadow.scheduler.util.HttpJobExecutor}
 */
public class HttpJobExecutorTest {
    private static final String URI = "http://www.google.com";
    private static final Map<String, Object> REQUEST_MAP = ImmutableMap.<String, Object>of(
            "content", "blah");
    private static final HttpEntity<Map<String, Object>> REQUEST_BODY =
            new HttpEntity<>(REQUEST_MAP);
    @Injectable private RestTemplate restTemplate;
    @Injectable private JobDAO jobDao;
    @Tested private HttpJobExecutor jobExecutor;

    @Test
    public void testExecuteJob() {
        new Expectations() {{
            restTemplate.exchange(URI, HttpMethod.GET, REQUEST_BODY, Map.class);
            result = new ResponseEntity<>(REQUEST_MAP, HttpStatus.OK);
        }};
        Job job = new JobImpl(new TaskImpl(URI, "GET", REQUEST_MAP,
                Lists.newArrayList(new ResponseCodeRange(200, 200))), "now");
        jobExecutor.execute(job);
    }

    @Test
    public void testExecuteJobNoExpectedResponse() {
        new Expectations() {{
            restTemplate.exchange(URI, HttpMethod.GET, REQUEST_BODY, Map.class);
            result = new ResponseEntity<>(REQUEST_MAP, HttpStatus.INTERNAL_SERVER_ERROR);
        }};
        Job job = new JobImpl(new TaskImpl(URI, "GET", REQUEST_MAP,
                Lists.<ResponseCodeRange>newArrayList()), "now");
        jobExecutor.execute(job);
    }

    @Test(expectedExceptions = MessageArgumentException.class)
    public void testExecuteJobUnexpectedResponse() {
        new Expectations() {{
            restTemplate.exchange(URI, HttpMethod.GET, REQUEST_BODY, Map.class);
            // NOTE: Spring Rest template will throw HttpClientErrorException or
            // HttpServerErrorException at run time the restTemplate in test is a mock,
            // so it does not
            result = new ResponseEntity<>(REQUEST_MAP, HttpStatus.INTERNAL_SERVER_ERROR);
        }};
        Job job = new JobImpl(new TaskImpl(URI, "GET", REQUEST_MAP,
                Lists.newArrayList(new ResponseCodeRange(200, 200))), "now");
        jobExecutor.execute(job);
    }

    @Test
    public void testExecuteJobRestClientException() {
        final Job job = new JobImpl(new TaskImpl(URI, "GET", REQUEST_MAP,
                Lists.newArrayList(new ResponseCodeRange(200, 200))), "now");
        job.setStatus(Job.Status.RUNNING);
        final Job failedJob = new JobImpl(new TaskImpl(URI, "GET", REQUEST_MAP,
                Lists.newArrayList(new ResponseCodeRange(200, 200))), "now");
        failedJob.setStatus(Job.Status.ERROR);
        new Expectations() {{
            jobDao.updateJob(job);
            restTemplate.exchange(URI, HttpMethod.GET, REQUEST_BODY, Map.class);
            result = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed");
            jobDao.updateJob(failedJob);
        }};
        jobExecutor.execute(job);
    }

    @Test(expectedExceptions = Exception.class)
    public void testExecuteJobException() {
        final Job job = new JobImpl(new TaskImpl(URI, "GET", REQUEST_MAP,
                Lists.newArrayList(new ResponseCodeRange(200, 200))), "now");
        new Expectations() {{
            restTemplate.exchange(URI, HttpMethod.GET, REQUEST_BODY, Map.class);
            result = new Exception("Failed");
        }};
        jobExecutor.execute(job);
    }
}
