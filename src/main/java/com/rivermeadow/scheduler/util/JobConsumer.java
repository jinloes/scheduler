package com.rivermeadow.scheduler.util;

import com.rivermeadow.scheduler.model.Job;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Consumes a job from the queue and executes it.
 */
@Component
public class JobConsumer implements QueueConsumer<Job> {
    private static final Logger logger = LoggerFactory.getLogger(JobConsumer.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void consumeMessage(Job job) throws Exception {
        // If the node goes down in this method, the job will be readded to the queue.
        logger.debug("Consumed message" + job);
        JobExecutor executor = (JobExecutor) applicationContext.getBean(job.getUriScheme());
        executor.execute(job);
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        System.out.println("State changed" + newState);
    }
}
