package com.rivermeadow.scheduler.util;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.rivermeadow.scheduler.dao.JobDAO;
import com.rivermeadow.scheduler.exception.MessageArgumentException;
import com.rivermeadow.scheduler.model.Job;
import com.rivermeadow.scheduler.model.ResponseCodeRange;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
    private final String apiUsername;
    private final String apiPassword;

    @Autowired
    public HttpJobExecutor(RestTemplate restTemplate, JobDAO jobDAO,
            @Qualifier("apiUsername") String apiUsername,
            @Qualifier("apiPassword") String apiPassword) {
        super(jobDAO, new HttpJobListener(jobDAO));
        this.restTemplate = restTemplate;
        this.apiUsername = apiUsername;
        this.apiPassword = apiPassword;
    }

    @Override
    protected void executeJob(Job job) {
        String methodStr = job.getTask().getMethod().toLowerCase();
        //TODO(jinloes) should we support GET?
        HttpMethod method = HttpMethod.valueOf(methodStr.toUpperCase());
        // Right now this is tied to system params from the scheduler
        // We might want a client to provide the auth as part of the job request
        ApiRequest request = ApiRequest.builder()
                .withBasicAuthUsername(apiUsername)
                .withBasicAuthPassword(apiPassword)
                .withBody(job.getTask().getBody())
                .build();
        ResponseEntity<Map> response = restTemplate.exchange(job.getTask().getUri(),
                method, request, Map.class);
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
        throw new MessageArgumentException(ErrorCodes.JOB_RESPONSE_CODE_UNEXPECTED, statusCode,
                expectedResponseCodes.toString());
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
                jobDAO.updateStatus(job.getId(), Job.Status.ERROR);
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                // Wrap the exception
                //TODO(jinloes) see if there's a better way to wrap this exception
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Creates a request to be sent to the API.
     */
    private static class ApiRequest extends HttpEntity<Object> {

        private ApiRequest(HttpHeaders httpHeaders, Object body) {
            super(body, httpHeaders);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private static final String BASIC_AUTH_STR_FMT = "%s:%s";
            private String basicAuthUsername;
            private String basicAuthPassword;
            private Object body;

            public Builder withBasicAuthUsername(String username) {
                this.basicAuthUsername = username;
                return this;
            }

            public Builder withBasicAuthPassword(String password) {
                this.basicAuthPassword = password;
                return this;
            }

            private Builder withBody(Object body) {
                this.body = body;
                return this;
            }

            public ApiRequest build() {
                HttpHeaders httpHeaders = new HttpHeaders();
                if(StringUtils.isNotEmpty(basicAuthUsername) &&
                        StringUtils.isNotEmpty(basicAuthPassword)) {
                    String authStr = String.format(BASIC_AUTH_STR_FMT, basicAuthUsername,
                            basicAuthPassword);
                    String encodedAuth = Base64.encodeBase64String(
                            authStr.getBytes(Charset.forName("US-ASCII")));
                    String authHeader = "Basic " + encodedAuth;
                    httpHeaders.set("Authorization", authHeader);
                }
                return new ApiRequest(httpHeaders, body);
            }
        }
    }
}
