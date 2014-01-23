package com.rivermeadow.scheduler.web;

import javax.validation.Valid;

import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.web.JobController;
import com.rivermeadow.scheduler.model.JobImpl;

import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class JobControllerImpl implements JobController<JobImpl> {
    private final DistributedQueue<Job> jobQueue;

    @Autowired
    public JobControllerImpl(final DistributedQueue<Job> jobQueue) {
        this.jobQueue = jobQueue;
    }

    String getTasks() {
        return "Hello World!";
    }

    @Override
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity scheduleJob(@RequestBody @Valid final JobImpl job) {
        try {
            jobQueue.put(job);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to add task to queue");
        }
        return new ResponseEntity("Task scheduled!", HttpStatus.CREATED);
    }
}