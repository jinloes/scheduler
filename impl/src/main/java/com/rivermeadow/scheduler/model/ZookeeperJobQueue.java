package com.rivermeadow.scheduler.model;

import java.io.IOException;

import javax.annotation.PreDestroy;

import com.rivermeadow.api.exception.MessageArgumentException;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.util.ErrorCodes;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A Zookeeper implementation of {@link JobQueue}.
 */
@Component
public class ZookeeperJobQueue implements JobQueue {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperJobQueue.class);
    private static final String JOBS_PATH = "/jobs";
    private static final String JOB_LOCKS_PATH = "/locks";
    private final DistributedQueue<Job> jobQueue;

    @Autowired
    public ZookeeperJobQueue(CuratorFramework curator, QueueConsumer<Job> jobQueueConsumer,
            QueueSerializer<Job> serializer) {
        jobQueue = QueueBuilder.builder(curator, jobQueueConsumer, serializer, JOBS_PATH)
                .lockPath(JOB_LOCKS_PATH)
                .buildQueue();
        try {
            jobQueue.start();
        } catch (Exception e) {
            throw new MessageArgumentException(ErrorCodes.JOB_QUEUE_CREATE_FAILED, e);
        }
    }

    @PreDestroy
    public void cleanUp() {
        if (jobQueue != null) {
            try {
                jobQueue.close();
            } catch (IOException e) {
                logger.error("Failed to close job queue.", e);
            }
        }
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
