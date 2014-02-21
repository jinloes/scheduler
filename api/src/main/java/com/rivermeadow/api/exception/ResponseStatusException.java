package com.rivermeadow.api.exception;

import com.rivermeadow.api.util.ErrorCodes;

import org.springframework.http.HttpStatus;

/**
 * Designates that an exception will contain a {@link HttpStatus}.
 */
public class ResponseStatusException extends RuntimeException {
    private HttpStatus httpStatus;

    public ResponseStatusException(ErrorCodes errorCode, HttpStatus httpStatus) {
        this(errorCode, httpStatus, null);
    }

    public ResponseStatusException(ErrorCodes errorCodes) {
        this(errorCodes, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseStatusException(ErrorCodes errorCode, HttpStatus httpStatus, Throwable t) {
        super(errorCode.getErrorCode(), t);
        this.httpStatus = httpStatus;
    }

    /**
     * Returns the http status that should be used.
     *
     * @return http status
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
