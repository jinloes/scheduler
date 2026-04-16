package com.scheduler.scheduler;

import com.scheduler.service.JobService;
import java.util.concurrent.locks.Lock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically polls Redis for jobs whose scheduled time has passed and moves them to the execution
 * queue. A distributed Redis lock prevents duplicate polling across multiple instances.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobScheduler {

  private static final String POLL_LOCK_KEY = "scheduler:poll";

  private final JobService jobService;
  private final LockRegistry lockRegistry;

  /** Polls for due jobs at the configured interval. Skips the poll if the lock is unavailable. */
  @Scheduled(fixedDelayString = "${scheduler.poll-rate-ms:5000}")
  public void pollForDueJobs() {
    Lock lock = lockRegistry.obtain(POLL_LOCK_KEY);
    if (lock.tryLock()) {
      try {
        jobService.processReadyJobs();
      } finally {
        lock.unlock();
      }
    }
  }
}
