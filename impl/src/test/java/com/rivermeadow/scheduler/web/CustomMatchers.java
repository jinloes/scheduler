package com.rivermeadow.scheduler.web;

import java.util.Map;
import java.util.UUID;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

/**
 * Custom matchers for testing.
 */
public class CustomMatchers {
    /**
     * Creates a {@link IsUuid} matcher.
     *
     * @return uuid matcher
     */
    public static IsUuid isUuid() {
        return new IsUuid();
    }

    /**
     * Create a {@link IsErrorField} matcher.
     *
     * @param expectedErrorField expected error field
     *
     * @return
     */
    public static IsErrorField isErrorField(Map<String, String> expectedErrorField) {
        return new IsErrorField(expectedErrorField);
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
            } catch (Exception e) {
                // Do nothing allow the matcher to fail
            }
            return matches;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("valid uuid");
        }
    }

    /**
     * Matcher for checking an error field returned by a validation failure.
     * An error field has the structure:
     * <pre>
     *     {@code
     *          {
     *              "field": "fieldName",
     *              "message": "error message"
     *          }
     *     }
     * </pre>
     *
     * The 'field' string will be checked for exact match.
     * The 'message' field will checked that the actual 'message' contains the expected 'message'.
     */
    public static class IsErrorField extends BaseMatcher<Map<String, String>> {
        private static final String FIELD_KEY = "field";
        private static final String MESSAGE_KEY = "message";
        private final Map<String, String> expectedErrorField;

        public IsErrorField(final Map<String, String> expectedErrorField) {
            this.expectedErrorField = expectedErrorField;
        }

        @Override
        public boolean matches(Object item) {
            try {
                assertThat(item, instanceOf(Map.class));
                Map<String, String> map = (Map<String, String>) item;
                assertThat(map, hasEntry(equalTo(FIELD_KEY),
                        equalTo(expectedErrorField.get(FIELD_KEY))));
                assertThat(map, hasEntry(equalTo(MESSAGE_KEY),
                        containsString(expectedErrorField.get(MESSAGE_KEY))));
                return true;
            } catch (Throwable t) {
                return false;
            }
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(expectedErrorField.toString());
        }
    }
}
