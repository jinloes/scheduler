package com.rivermeadow.scheduler.util;

import com.rivermeadow.api.model.Job;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by jinloes on 1/22/14.
 */
@Component
public class ScheduledJobListener implements JobListener {
    private static final String NAME = "Scheduled Job Listener";
    private final CuratorFramework curatorFramework;

    @Autowired
    public ScheduledJobListener(final CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {

    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        //TODO(jinloes) remove job from execution list
        try {
            curatorFramework.delete().forPath(String.format("/execution/%s", context.getJobDetail().getKey().getName()));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to remove job from execution list");
        }
        //TOOD(jinloes) archive job information
        System.out.println("Job finished archiving data");
    }
}
