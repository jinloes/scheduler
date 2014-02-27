package com.rivermeadow.api.exception;

import com.rivermeadow.api.util.ErrorCodes;

import org.springframework.http.HttpStatus;

/**
 * Designates that an exception will have message arguments.
 * Example usage:
 * Message with {@link ErrorCodes} and {@link HttpStatus}
 * <pre>
 *     {@code
 *          new MessageArgumentException(ErrorCodes.JOB_NOT_FOUND, HttpStatus.NOT_FOUND, "123")
 *     }
 * </pre>
 *
 * would have the response be a 404 and message: No job was found for id: 123.
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

    /**
     * Create an exception that will have it's message codes resolved
     *
     * @param errorCode error message code to resolve
     * @param httpStatus http status to return for the exception
     * @param t throwable to wrap
     * @param args message arguments
     */
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
