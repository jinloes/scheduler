package com.rivermeadow.scheduler.model;

/**
 * DTO for returning validation error messages.
 */
public class FieldErrorDTO {
    private final String field;
    private final String message;

    private FieldErrorDTO(Builder builder) {
        this.field = builder.field;
        this.message = builder.message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Builder a {@link FieldErrorDTO}.
     */
    public static class Builder {
        private String field;
        private String message;

        private Builder() {
        }

        public Builder withField(String field) {
            this.field = field;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public FieldErrorDTO build() {
            return new FieldErrorDTO(this);
        }
    }
}
