package com.rivermeadow.api.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import org.apache.commons.lang3.Range;

/**
 * Serializes a {@link Range} object.
 */
public class RangeSerializer extends JsonSerializer<Range<Integer>> {
    private static final String RANGE_FORMAT = "%s-%s";
    @Override
    public void serialize(Range<Integer> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        Integer min = value.getMinimum();
        Integer max = value.getMaximum();
        jgen.writeString(String.format(RANGE_FORMAT, min, max));

    }
}
