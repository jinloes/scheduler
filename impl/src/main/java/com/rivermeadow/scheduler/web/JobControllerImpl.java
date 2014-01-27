package com.rivermeadow.scheduler.web;

import javax.validation.Valid;

import com.rivermeadow.api.dao.JobDAO;
import com.rivermeadow.api.web.JobController;
import com.rivermeadow.api.web.JsonPost;
import com.rivermeadow.scheduler.model.JobImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobControllerImpl implements JobController<JobImpl> {
    private final JobDAO jobDao;

    @Autowired
    public JobControllerImpl(final JobDAO jobDao) {
        this.jobDao = jobDao;
    }

    @Override
    @JsonPost
    public ResponseEntity scheduleJob(@RequestBody @Valid final JobImpl job) {
        jobDao.addJob(job);
        //TODO(jinloes) add id and link to job
        return new ResponseEntity("Task scheduled!", HttpStatus.CREATED);
    }
}