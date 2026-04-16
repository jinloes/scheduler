package com.scheduler.scheduler;

/** Thrown for failures that should not be retried, e.g. HTTP 4xx client errors. */
class NonRetryableJobException extends RuntimeException {

  NonRetryableJobException(String message) {
    super(message);
  }
}
