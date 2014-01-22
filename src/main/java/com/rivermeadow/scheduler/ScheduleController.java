package com.rivermeadow.scheduler;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for scheduling tasks.
 */
@RestController
@RequestMapping("/schedules")
public class ScheduleController {
    private final Scheduler scheduler;

    @Autowired
    public ScheduleController(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    String getTasks() {
        return "Hello World!";
    }

    @RequestMapping(method = RequestMethod.POST)
    String scheduleTask() {

        CuratorFramework framework = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .namespace("scheduler")
                .build();
        framework.start();
        JobDetail jobDetail = JobBuilder.newJob(TestJob.class)
                .withIdentity("my id")
                .build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger id")
                .startNow()
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return "Task scheduled!";
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan
    public static class Application {
        @Bean
        public Scheduler getScheduler() {
            try {
                Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                return scheduler;
            } catch (SchedulerException e) {
                throw new RuntimeException("Could not get scheduler.");
            }
        }
        public static void main(String[] args) throws Exception {
            SpringApplication.run(ScheduleController.class, args);
        }
    }

    public static class TestJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Hello");
        }
    }
}
