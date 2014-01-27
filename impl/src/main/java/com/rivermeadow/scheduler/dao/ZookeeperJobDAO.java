package com.rivermeadow.scheduler.dao;

import com.rivermeadow.api.dao.JobDAO;
import com.rivermeadow.api.model.Job;

import org.apache.curator.framework.recipes.queue.DistributedDelayQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Zookeeper implementation of {@link com.rivermeadow.api.dao.JobDAO}.
 */
@Repository
public class ZookeeperJobDAO implements JobDAO {
    private final DistributedDelayQueue<Job> jobQueue;

    @Autowired
    public ZookeeperJobDAO(final DistributedDelayQueue<Job> jobQueue) {
        this.jobQueue = jobQueue;
    }

    @Override
    public void addJob(Job job) {
        try {
            jobQueue.put(job, job.getScheduleDate().getMillis());
        } catch (Exception e) {
            //TODO(jinloes) clean up and add better error message
            e.printStackTrace();
            throw new RuntimeException("Failed to add task to queue");
        }
    }
}
