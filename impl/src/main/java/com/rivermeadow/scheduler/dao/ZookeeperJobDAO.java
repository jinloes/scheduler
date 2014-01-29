package com.rivermeadow.scheduler.dao;

import com.rivermeadow.api.dao.JobDAO;
import com.rivermeadow.api.model.Job;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.DistributedDelayQueue;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
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
    public ZookeeperJobDAO(final DistributedDelayQueue<Job> jobQueue, final CuratorFramework curatorFramework,
            final QueueSerializer<Job> serializer) {
        this.jobQueue = jobQueue;
        this.curatorFramework = curatorFramework;
        this.serializer = serializer;
    }

    @Override
    public void checkExists(String id) {
        String jobPath = getJobArchivePath(id);
        try {
            if(curatorFramework.checkExists().forPath(jobPath) == null) {
                throw new DataRetrievalFailureException(String.format("Failed to get job for id: %s", id));
            }
        } catch (DataRetrievalFailureException e) {
            throw e;
        } catch (Exception e) {
            //TODO(jinloes) use messages file
            e.printStackTrace();
            throw new RuntimeException(String.format("Failed to check existence of path: %s", jobPath));
        }
    }

    @Override
    public void queueJob(Job job) {
        try {
            jobQueue.put(job, job.getSchedule().getMillis());
        } catch (Exception e) {
            //TODO(jinloes) clean up and add better error message
            e.printStackTrace();
            throw new RuntimeException("Failed to add task to queue");
        }
    }

    @Override
    public void saveJob(Job job) {
        try {
            curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .forPath(getJobArchivePath(job.getId().toString()), serializer.serialize(job));
        } catch (Exception e) {
            //TODO(jinloes) use messages file
            e.printStackTrace();
            throw new RuntimeException("Failed to archive job.");
        }

    }

    @Override
    public Job getJob(String id) {
        try {
            byte[] bytes = curatorFramework.getData().forPath(getJobArchivePath(id));
            return serializer.deserialize(bytes);
        } catch (Exception e) {
            //TODO(jinloes) use messages file
            e.printStackTrace();
            throw new RuntimeException(String.format("Failed to get job for id: %s", id));
        }
    }

    @Override
    public void updateJob(Job job) {
        String jobId = job.getId().toString();
        try {
            curatorFramework.setData().forPath(getJobArchivePath(jobId), serializer.serialize(job));
        } catch (Exception e) {
            //TODO(jinloes) use messages file
            e.printStackTrace();
            throw new RuntimeException(String.format("Failed to update job for id: %s", jobId));
        }
    }

    private String getJobArchivePath(String id) {
        return String.format(JOB_ARCHIVE_PATH, id);
    }
}
