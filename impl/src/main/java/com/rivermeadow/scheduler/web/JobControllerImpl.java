package com.rivermeadow.scheduler.web;

import java.util.Map;

import javax.validation.Valid;

import com.google.common.collect.ImmutableMap;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.service.JobService;
import com.rivermeadow.api.web.JobController;
import com.rivermeadow.api.web.JsonPost;
import com.rivermeadow.scheduler.model.JobImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(JobController.ROOT_JOB_PATH)
public class JobControllerImpl implements JobController<JobImpl> {
    private final JobService jobService;

    @Autowired
    public JobControllerImpl(final JobService jobService) {
        this.jobService = jobService;
    }

    @Override
    @RequestMapping(value = "/{jobId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody Job getJob(@PathVariable("jobId") final String jobId) {
        return jobService.getJob(jobId);
    }

    @Override
    @JsonPost
    public ResponseEntity scheduleJob(@RequestBody @Valid final JobImpl job) {
        jobService.queueJob(job);
        String jobId = job.getId().toString();
        Map<String, Object> response = ImmutableMap.<String, Object>of("id", jobId,
                "link", getJobLink(jobId));
        return new ResponseEntity(response, HttpStatus.CREATED);
    }

    private String getJobLink(String jobId) {
        return String.format(JOB_LINK, ApplicationInitializer.APPLICATION_ROOT_PATH, jobId);
    }
}