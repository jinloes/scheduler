package com.rivermeadow.scheduler.util;

import com.rivermeadow.api.model.Job;

import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

/**
 * Serializes/Deserializes jobs to/from Zookeeper.
 */
@Component
public class JobQueueSerializer implements QueueSerializer<Job> {
    @Override
    public byte[] serialize(Job item) {
        return SerializationUtils.serialize(item);
    }

    @Override
    public Job deserialize(byte[] bytes) {
        return (Job) SerializationUtils.deserialize(bytes);
    }
}
