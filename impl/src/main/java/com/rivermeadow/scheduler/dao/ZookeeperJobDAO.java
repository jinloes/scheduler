package com.rivermeadow.scheduler.dao;

import com.rivermeadow.api.dao.JobDAO;
import com.rivermeadow.api.exception.MessageArgumentException;
import com.rivermeadow.api.exception.NotFoundException;
import com.rivermeadow.api.exception.ResponseStatusException;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.util.ErrorCodes;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.DistributedDelayQueue;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Zookeeper implementation of {@link com.rivermeadow.api.dao.JobDAO}.
 */
@Repository
public class ZookeeperJobDAO implements JobDAO {
    public static final String JOB_ARCHIVE_PATH = "/jobs/archive/%s";
    private final DistributedDelayQueue<Job> jobQueue;
    private final CuratorFramework curatorFramework;
    private final QueueSerializer<Job> serializer;

    @Autowired
    public ZookeeperJobDAO(final DistributedDelayQueue<Job> jobQueue,
            final CuratorFramework curatorFramework, final QueueSerializer<Job> serializer) {
        this.jobQueue = jobQueue;
        this.curatorFramework = curatorFramework;
        this.serializer = serializer;
    }

    @Override
    public void checkExists(String id) throws Exception {
        String jobPath = getJobArchivePath(id);
        if(curatorFramework.checkExists().forPath(jobPath) == null) {
            throw new NotFoundException(ErrorCodes.JOB_NOT_FOUND, id);
        }
    }

    @Override
    public void queueJob(Job job) {
        try {
            jobQueue.put(job, job.getSchedule().getMillis());
        } catch (Exception e) {
            throw new ResponseStatusException(ErrorCodes.JOB_QUEUE_FAILED);
        }
    }

    @Override
    public void saveJob(Job job) {
        try {
            curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .forPath(getJobArchivePath(job.getId().toString()), serializer.serialize(job));
        } catch (Exception e) {
            throw new ResponseStatusException(ErrorCodes.JOB_SAVE_FAILED);
        }
    }

    @Override
    public Job getJob(String id) throws Exception {
        byte[] bytes = curatorFramework.getData().forPath(getJobArchivePath(id));
        return serializer.deserialize(bytes);
    }

    @Override
    public void updateJob(Job job) {
        String jobId = job.getId().toString();
        try {
            curatorFramework.setData().forPath(getJobArchivePath(jobId), serializer.serialize(job));
        } catch (Exception e) {
            throw new MessageArgumentException(ErrorCodes.JOB_UPDATE_FAILED, jobId);
        }
    }

    private String getJobArchivePath(String id) {
        return String.format(JOB_ARCHIVE_PATH, id);
    }
}
