package com.rivermeadow.scheduler.util;

/**
 * Error codes that map to the message.properties file.
 */
public enum ErrorCodes {
    JOB_NOT_FOUND("Job.not_found"),
    JOB_GET_FAILED("Job.get.failed"),
    JOB_QUEUE_FAILED("Job.queue.failed"),
    JOB_QUEUE_CREATE_FAILED("Job.queue.create.failed"),
    JOB_SAVE_FAILED("Job.save.failed"),
    JOB_UPDATE_FAILED("Job.update.failed"),
    JOB_RESPONSE_CODE_UNEXPECTED("Job.response_code.unexpected");

    private final String errorCode;

    private ErrorCodes(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
