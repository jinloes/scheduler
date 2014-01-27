package com.rivermeadow.api.validation;

import java.net.URI;
import java.net.URISyntaxException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * See {@link com.rivermeadow.api.validation.UriValidator}.
 */
public class UriValidator implements ConstraintValidator<Uri, String> {
    @Override
    public void initialize(Uri constraintAnnotation) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            switch (scheme) {
                case "http":
                    return true;
            }
        } catch (URISyntaxException e) { }
        return false;
    }
}
