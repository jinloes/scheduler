package com.rivermeadow.scheduler.model;

/**
 * A DTO for returning error messages.
 */
public final class ErrorMessageDTO {
    private final String message;

    public ErrorMessageDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
