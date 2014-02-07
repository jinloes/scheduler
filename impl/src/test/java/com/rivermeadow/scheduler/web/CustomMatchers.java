package com.rivermeadow.scheduler.web;

import java.util.UUID;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Custom matchers for testing.
 */
public class CustomMatchers {
    public static IsUuid isUuid() {
        return new IsUuid();
    }
    /**
     * A matcher for validating {@link java.util.UUID}s.
     */
    public static class IsUuid extends BaseMatcher<String> {

        @Override
        public boolean matches(Object o) {
            boolean matches = false;
            try {
                UUID.fromString(o.toString());
                matches = true;
            } catch(Exception e) {
                // Do nothing allow the matcher to fail
            }
            return matches;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("valid uuid");
        }
    }
}
