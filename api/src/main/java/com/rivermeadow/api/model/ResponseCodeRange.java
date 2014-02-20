package com.rivermeadow.api.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;
import com.rivermeadow.api.util.RangeSerializer;

import org.apache.commons.lang3.Range;

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
        return Objects.toStringHelper(this)
                .add("range", range)
                .toString();
    }
}
