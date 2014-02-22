package com.rivermeadow.api.model;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Models a response code range. A response code range is used to verify a response code from
 * an http job.
 */
public class ResponseCodeRange implements Serializable {
    private static final long serialVersionUID = 1207663786461510187L;
    private final Range<Integer> range;

    public ResponseCodeRange(@JsonProperty("start") Integer start,
            @JsonProperty("end") Integer end) {
        start = start == null ? Integer.MIN_VALUE : start;
        end = end == null ? Integer.MAX_VALUE : end;
        range = Range.between(start, end);
    }

    public boolean contains(Integer value) {
        return range.contains(value);
    }

    public Integer getStart() {
        return range.getMinimum();
    }

    public Integer getEnd() {
        return range.getMaximum();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("range", range)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(range);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ResponseCodeRange other = (ResponseCodeRange) obj;
        return Objects.equals(this.range, other.range);
    }
}
