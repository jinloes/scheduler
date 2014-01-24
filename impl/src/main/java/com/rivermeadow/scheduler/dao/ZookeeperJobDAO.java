package com.rivermeadow.scheduler.dao;

import com.rivermeadow.api.dao.JobDAO;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.scheduler.util.JobQueueSerializer;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.DistributedDelayQueue;
import org.apache.curator.framework.recipes.queue.DistributedPriorityQueue;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
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
            e.printStackTrace();
            throw new RuntimeException("Failed to add task to queue");
        }
    }
}
