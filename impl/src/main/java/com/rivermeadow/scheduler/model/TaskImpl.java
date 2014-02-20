package com.rivermeadow.scheduler.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.rivermeadow.api.model.ResponseCodeRange;
import com.rivermeadow.api.model.Task;

/**
 * {@link Task} implementation.
 */
public class TaskImpl implements Task {
    private static final long serialVersionUID = -8683851579840355977L;
    private final String uri;
    private final String method;
    private final Map<String, Object> body;
    private final List<ResponseCodeRange> expectedRanges;

    @JsonCreator
    public TaskImpl(@JsonProperty("uri") final String uri,
            @JsonProperty("method") final String method,
            @JsonProperty("body") final Map<String, Object> body,
            @JsonProperty("response_code_ranges") final List<ResponseCodeRange> expectedRanges) {
        this.uri = uri;
        this.method = method;
        this.body = body;
        this.expectedRanges = expectedRanges;
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
    @JsonProperty("response_code_ranges")
    public List<ResponseCodeRange> getResponseCodeRanges() {
        return expectedRanges;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("uri", uri)
                .add("method", method)
                .add("body", body)
                .add("expectedRanges", expectedRanges)
                .toString();
    }
}