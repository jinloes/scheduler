package com.rivermeadow.api.model;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rivermeadow.api.util.RangeSerializer;
import com.rivermeadow.api.validation.Uri;

import org.apache.commons.lang3.Range;

/**
 * A task that a {@link Job} will execute.
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
    @JsonProperty("expected_range")
    @JsonSerialize(using = RangeSerializer.class)
    Range<Integer> getExpectedRange();
}
