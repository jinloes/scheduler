package com.rivermeadow.api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

/**
 * Validates a job's target uri.
 */
@Documented
@Constraint(validatedBy = UriValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Uri {
    //TODO(jinloes) use ValidationMessage.properties file
    public abstract String message() default "Invalid uri scheme. Supported schemes are http.";

    public abstract Class[] groups() default {};

    public abstract Class[] payload() default {};
}
