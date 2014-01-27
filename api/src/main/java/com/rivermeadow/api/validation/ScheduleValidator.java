package com.rivermeadow.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.rivermeadow.api.model.Job;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.ISODateTimeFormat;

/**
 * See {@link com.rivermeadow.api.validation.Schedule}. Validats that the 'schedule' field is either the value 'now'
 * or an ISO8601 datetime string.
 */
public class ScheduleValidator implements ConstraintValidator<Schedule, String> {
    @Override
    public void initialize(Schedule constraintAnnotation) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Must equal 'now' or ISO8601 date time
        if(StringUtils.equalsIgnoreCase(Job.NOW, value)) {
            return true;
        }
        try {
            ISODateTimeFormat.dateOptionalTimeParser().parseDateTime(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
