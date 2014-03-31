package com.rivermeadow.scheduler.util;

import java.nio.charset.Charset;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.rivermeadow.scheduler.dao.JobDAO;
import com.rivermeadow.scheduler.exception.MessageArgumentException;
import com.rivermeadow.scheduler.model.Job;
import com.rivermeadow.scheduler.model.ResponseCodeRange;
import com.rivermeadow.scheduler.model.JobImpl;
import com.rivermeadow.scheduler.model.TaskImpl;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
    private static final HttpEntity<Map<String, Object>> REQUEST_BODY;
    @Injectable private RestTemplate restTemplate;
    @Injectable private JobDAO jobDao;
    @Tested private HttpJobExecutor jobExecutor;

    static {
        String auth = "admin@rivermeadow.com" + ":" + "secret";
        String encodedAuth = Base64.encodeBase64String(
                auth.getBytes(Charset.forName("US-ASCII")));
        String authHeader = "Basic " + new String(encodedAuth);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        REQUEST_BODY = new HttpEntity<>(REQUEST_MAP, headers);
    }

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
        new Expectations() {{
            jobDao.updateStatus(job.getId(), Job.Status.RUNNING);
            restTemplate.exchange(URI, HttpMethod.GET, REQUEST_BODY, Map.class);
            result = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed");
            jobDao.updateStatus(job.getId(), Job.Status.ERROR);
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
