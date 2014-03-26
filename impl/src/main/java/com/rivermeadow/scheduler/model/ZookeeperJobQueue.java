package com.rivermeadow.scheduler.model;

import com.rivermeadow.api.exception.MessageArgumentException;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.util.ErrorCodes;

import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A Zookeeper implementation of {@link JobQueue}.
 */
@Component
public class ZookeeperJobQueue implements JobQueue {
    private final DistributedQueue<Job> jobQueue;

    @Autowired
    public ZookeeperJobQueue(DistributedQueue<Job> jobQueue) {
        this.jobQueue = jobQueue;
    }

    @Override
    public void queueJob(Job job) {
        try {
            jobQueue.put(job);
        } catch (Exception e) {
            throw new MessageArgumentException(ErrorCodes.JOB_QUEUE_FAILED, e);
        }
    }
}
