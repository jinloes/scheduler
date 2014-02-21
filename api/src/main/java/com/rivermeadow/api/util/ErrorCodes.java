package com.rivermeadow.api.util;

/**
 * Error codes that map to the message.properties file.
 */
public enum ErrorCodes {
    JOB_NOT_FOUND("Job.not_found"),
    JOB_GET_FAILED("Job.get.failed"),
    JOB_QUEUE_FAILED("Job.queue.failed"),
    JOB_SAVE_FAILED("Job.save.failed"),
    JOB_UPDATE_FAILED("Job.update.failed");

    private final String errorCode;

    private ErrorCodes(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
