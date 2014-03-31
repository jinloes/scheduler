package com.rivermeadow.scheduler.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("uri", uri)
                .append("method", method)
                .append("body", body)
                .append("expectedRanges", expectedRanges)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, method, body, expectedRanges);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TaskImpl other = (TaskImpl) obj;
        return Objects.equals(this.uri, other.uri) &&
                Objects.equals(this.method, other.method) &&
                Objects.equals(this.body, other.body) &&
                Objects.equals(this.expectedRanges, other.expectedRanges);
    }
}