package com.rivermeadow.api.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.rivermeadow.api.model.Job;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Created by jinloes on 1/23/14.
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
