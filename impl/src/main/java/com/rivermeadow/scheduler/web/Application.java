package com.rivermeadow.scheduler.web;

import java.util.concurrent.TimeUnit;

import com.rivermeadow.api.model.Job;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.queue.DistributedQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.curator.retry.RetryUntilElapsed;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.EverythingMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration and bean definitions.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.rivermeadow.scheduler"})
public class Application {
    @Bean
    @Autowired
    public Scheduler getScheduler(final JobListener jobListener) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.getListenerManager().addJobListener(jobListener, EverythingMatcher.allJobs());
            scheduler.start();
            return scheduler;
        } catch (SchedulerException e) {
            throw new RuntimeException("Could not get scheduler.");
        }
    }

    @Bean
    public CuratorFramework getCurator() {
        CuratorFramework curator = CuratorFrameworkFactory.builder()
                .retryPolicy(new RetryUntilElapsed(
                        (int) TimeUnit.SECONDS.toMillis(5),
                        (int) TimeUnit.SECONDS.toSeconds(1)))
                .connectString("localhost:2181")
                .namespace("scheduler")
                .build();
        curator.start();
        return curator;
    }

    @Bean
    @Autowired
    public DistributedQueue<Job> getJobQueue(final CuratorFramework client, final QueueConsumer<Job> jobConsumer,
                                             final QueueSerializer<Job> serializer) {
        QueueBuilder<Job> builder = QueueBuilder.builder(client, jobConsumer, serializer, "/jobs");
        DistributedQueue<Job> jobQueue = builder.buildQueue();
        try {
            jobQueue.start();
        } catch (Exception e) {
            throw new RuntimeException("Could not create job queue.");
        }
        return jobQueue;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
