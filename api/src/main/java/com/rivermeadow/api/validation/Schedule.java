package com.rivermeadow.api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

/**
 * Validates a schedule field of a job.
 */
@Documented
@Constraint(validatedBy = ScheduleValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Schedule {
    //TODO(jinloes) use ValidationMessage.properties file
    public abstract String message() default "Invalid date. Must be ISO8601 format or value 'now'.";

    public abstract Class[] groups() default {};

    public abstract Class[] payload() default {};
}
