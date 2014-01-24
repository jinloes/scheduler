package com.rivermeadow.scheduler.util;

import com.rivermeadow.api.model.Job;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.state.ConnectionState;
import org.springframework.stereotype.Component;

/**
 * Created by jinloes on 1/22/14.
 */
@Component
public class JobConsumer implements QueueConsumer<Job> {

    @Override
    public void consumeMessage(Job job) throws Exception {
        //TODO(jinloes) figure out how to execute jobs
        // If the node goes down in this method, the job will be readded to the queue.
        System.out.println("Consumed message" + job);
        System.out.println("Running job.");
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        System.out.println("State changed" + newState);
    }
}
