package com.rivermeadow.api.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import javax.validation.Constraint;

/**
 * Validates a job's target uri.
 */
@Documented
@Constraint(validatedBy = UriValidator.class)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Uri {
    String message() default "Invalid uri scheme. Supported schemes are http.";
    Class[] groups() default {};
    Class[] payload() default {};

    /**
     * Schemes supported by the validator.
     *
     * @return supported schemes
     */
    String[] supportedSchemes() default {"http"};

}
