package com.rivermeadow.scheduler.model;

import java.util.Map;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.rivermeadow.api.model.Task;

import org.apache.commons.lang3.Range;

/**
 * {@link Task} implementation.
 */
public class TaskImpl implements Task {
    private static final Pattern RANGE_SEPARATOR = Pattern.compile("\\-");
    private final String uri;
    private final String method;
    private final Map<String, Object> body;
    private final Range<Integer> expectedRange;

    @JsonCreator
    public TaskImpl(@JsonProperty("uri") final String uri, @JsonProperty("method") final String method,
                    @JsonProperty("body") final Map<String, Object> body,
                    @JsonProperty("expected_range") final String expectedRange) {
        this.uri = uri;
        this.method = method;
        this.body = body;
        //TODO(jinloes) validate range
        String[] range = RANGE_SEPARATOR.split(expectedRange);
        this.expectedRange = Range.between(Integer.parseInt(range[0]), Integer.parseInt(range[1]));
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public Map<String, Object> getBody() {
        return body;
    }

    @Override
    public Range<Integer> getExpectedRange() {
        return expectedRange;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("uri", uri)
                .add("method", method)
                .add("body", body)
                .add("expectedRange", expectedRange)
                .toString();
    }
}