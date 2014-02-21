package com.rivermeadow.api.model;

/**
 * A DTO for returning error messages.
 */
public final class ErrorMessageDTO {
    private final String message;

    private ErrorMessageDTO(Builder builder) {
        this.message = builder.message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getMessage() {
        return message;
    }

    /**
     * A builder for {@linke ErrorMessageDTO}.
     */
    public static class Builder {
        private String message;

        private Builder() {
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public ErrorMessageDTO build() {
            return new ErrorMessageDTO(this);
        }
    }
}
