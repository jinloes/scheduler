package com.rivermeadow.api.validation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.google.common.collect.Sets;

import org.apache.commons.lang3.StringUtils;

/**
 * See {@link com.rivermeadow.api.validation.UriValidator}.
 */
public class UriValidator implements ConstraintValidator<Uri, String> {
    private Set<String> supportedSchemes;

    @Override
    public void initialize(Uri constraintAnnotation) {
        supportedSchemes = Sets.newHashSet(constraintAnnotation.supportedSchemes());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (StringUtils.isEmpty(scheme)) {
                return false;
            }
            return supportedSchemes.contains(scheme);
        } catch (URISyntaxException e) {
            // Fail validation
            return false;
        }
    }
}
