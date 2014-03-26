package com.rivermeadow.scheduler.web;

import javax.validation.Valid;

import com.google.common.collect.ImmutableMap;
import com.rivermeadow.api.exception.NotFoundException;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.service.JobService;
import com.rivermeadow.api.util.ErrorCodes;
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
    public
    @ResponseBody
    Job getJob(@PathVariable("jobId") final String jobId) {
        Job job = jobService.getJob(jobId);
        if (job == null) {
            throw new NotFoundException(ErrorCodes.JOB_NOT_FOUND, jobId);
        }
        return job;
    }

    @Override
    @JsonPost
    public ResponseEntity scheduleJob(@RequestBody @Valid final JobImpl job) {
        jobService.saveJob(job);
        String jobId = job.getId().toString();
        return new ScheduleJobResponse(jobId, getJobLink(jobId));
    }

    private String getJobLink(String jobId) {
        return String.format(JOB_LINK, ApplicationInitializer.APPLICATION_ROOT_PATH, jobId);
    }

    private static class ScheduleJobResponse extends ResponseEntity {
        public ScheduleJobResponse(String id, String link) {
            super(ImmutableMap.<String, Object>of(
                    "id", id,
                    "link", link), HttpStatus.CREATED);
        }
    }
}