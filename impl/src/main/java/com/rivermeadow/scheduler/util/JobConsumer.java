package com.rivermeadow.scheduler.util;

import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.util.JobExecutor;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.state.ConnectionState;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoader;

/**
 * Created by jinloes on 1/22/14.
 */
@Component
public class JobConsumer implements QueueConsumer<Job> {
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void consumeMessage(Job job) throws Exception {
        //TODO(jinloes) figure out how to execute jobs
        // If the node goes down in this method, the job will be readded to the queue.
        System.out.println("Consumed message" + job);
        JobExecutor executor = (JobExecutor) applicationContext.getBean(job.getUriScheme());
        executor.execute(job);
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        System.out.println("State changed" + newState);
    }
}
