package com.rivermeadow.scheduler.util;

import com.rivermeadow.api.model.Job;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.curator.framework.state.ConnectionState;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rivermeadow.scheduler.job.TestJob;

/**
 * Created by jinloes on 1/22/14.
 */
@Component
public class JobConsumer implements QueueConsumer<Job> {
    private final Scheduler scheduler;
    private final CuratorFramework curatorFramework;
    private final QueueSerializer<Job> serializer;

    @Autowired
    public JobConsumer(final Scheduler scheduler, final CuratorFramework curatorFramework,
                       final QueueSerializer<Job> serializer) {
        this.scheduler = scheduler;
        this.curatorFramework = curatorFramework;
        this.serializer = serializer;
    }

    @Override
    public void consumeMessage(Job job) throws Exception {
        System.out.println("Consumed message" + job);
        String value = job.getSchedule();
        JobDetail jobDetail = JobBuilder.newJob(TestJob.class).build();
        TriggerBuilder trigger = TriggerBuilder.newTrigger();
        if(StringUtils.equalsIgnoreCase(Job.NOW, value)) {
            trigger = trigger.startNow();
        } else {
            //TODO(jinloes) maybe move this to model
            DateTime scheduleDate = ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(value);
            trigger.startAt(scheduleDate.toDate());
        }
        try {
            curatorFramework.create().creatingParentsIfNeeded().forPath(String.format("/execution/%s", jobDetail.getKey().getName()),
                    serializer.serialize(job));
            scheduler.scheduleJob(jobDetail, trigger.build());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        System.out.println("State changed" + newState);
    }
}
