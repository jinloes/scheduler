package com.scheduler.scheduler;

/** Thrown when an HTTP callback fails, signalling the retry template to attempt again. */
class JobExecutionException extends RuntimeException {

  JobExecutionException(String message) {
    super(message);
  }

  JobExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
