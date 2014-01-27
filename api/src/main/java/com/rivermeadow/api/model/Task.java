package com.rivermeadow.api.model;

import java.io.Serializable;
import java.util.Map;

import com.rivermeadow.api.validation.Uri;

import org.apache.commons.lang3.Range;

/**
 * Created by jinloes on 1/23/14.
 */
public interface Task extends Serializable {
    /**
     * Returns the task's target uri.
     *
     * @return uri
     */
    @Uri
    String getUri();

    /**
     * Returns the task's request method
     *
     * @return request method
     */
    String getMethod();

    /**
     * Returns the task's payload body.
     *
     * @return payload
     */
    Map<String, Object> getBody();

    /**
     * Return the expected response code range;
     *
     * @return expected response code range
     */
    Range<Integer> getExpectedRange();
}
