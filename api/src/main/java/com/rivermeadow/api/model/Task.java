package com.rivermeadow.api.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.rivermeadow.api.validation.Uri;

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
     * Return the expected response code ranges;
     *
     * @return expected response code ranges
     */
    List<ResponseCodeRange> getResponseCodeRanges();
}
