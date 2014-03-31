package com.rivermeadow.scheduler.web;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

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
     * Creates a {@link IsBeforeOrEqualToDate} matcher.
     *
     * @param expectedDate expected date
     *
     * @return date matcher
     */
    public static IsBeforeOrEqualToDate isBeforeOrEqualToDate(Date expectedDate) {
        return new IsBeforeOrEqualToDate(expectedDate);
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

    /**
     * Checks if a date is before or equal to another
     */
    public static class IsBeforeOrEqualToDate extends BaseMatcher<Date> {
        private Date beforeDate;

        public IsBeforeOrEqualToDate(Date beforeDate) {
            this.beforeDate = beforeDate;
        }

        @Override
        public boolean matches(Object item) {
            Date actual = (Date) item;
            return actual.before(beforeDate) || actual.equals(beforeDate);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("is before or equal to " + beforeDate);
        }
    }
}
