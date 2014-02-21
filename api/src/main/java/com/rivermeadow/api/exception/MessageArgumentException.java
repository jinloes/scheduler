package com.rivermeadow.api.exception;

import com.rivermeadow.api.util.ErrorCodes;

import org.springframework.http.HttpStatus;

/**
 * Designates that an exception will have message arguments.
 */
public class MessageArgumentException extends ResponseStatusException {
    private final Object[] args;

    public MessageArgumentException(ErrorCodes errorCode, HttpStatus httpStatus, Object... args) {
        this(errorCode, httpStatus, null, args);
    }

    public MessageArgumentException(ErrorCodes errorCode, Object... args) {
        this(errorCode, HttpStatus.INTERNAL_SERVER_ERROR, null, args);
    }

    public MessageArgumentException(ErrorCodes errorCode, Throwable t, Object... args) {
        this(errorCode, HttpStatus.INTERNAL_SERVER_ERROR, t, args);
    }

    public MessageArgumentException(ErrorCodes errorCode, HttpStatus httpStatus, Throwable t,
            Object... args) {
        super(errorCode, httpStatus, t);
        this.args = args;
    }

    /**
     * Return the message arguments.
     *
     * @return message arguments
     */
    public Object[] getArgs() {
        return args;
    }
}
