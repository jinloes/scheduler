package com.rivermeadow.api.model;

/**
 * A DTO for returning error messages.
 */
public final class ErrorMessageDTO {
    private final String message;

    private ErrorMessageDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
