package com.rivermeadow.scheduler.exception;

import com.rivermeadow.scheduler.util.ErrorCodes;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a resource was not found.
 */
public class NotFoundException extends MessageArgumentException {
    public NotFoundException(ErrorCodes errorCode, Object... args) {
        super(errorCode, HttpStatus.NOT_FOUND, args);
    }
}
