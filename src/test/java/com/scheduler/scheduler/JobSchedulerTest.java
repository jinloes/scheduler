package com.scheduler.scheduler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scheduler.service.JobService;
import java.util.concurrent.locks.Lock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.support.locks.LockRegistry;

@ExtendWith(MockitoExtension.class)
class JobSchedulerTest {

  @Mock private JobService jobService;
  @Mock private LockRegistry lockRegistry;
  @Mock private Lock lock;

  private JobScheduler scheduler;

  @BeforeEach
  void setUp() {
    scheduler = new JobScheduler(jobService, lockRegistry);
    when(lockRegistry.obtain("scheduler:poll")).thenReturn(lock);
  }

  @Nested
  class PollForDueJobs {

    @Test
    void whenLockAcquired_callsProcessReadyJobs() {
      when(lock.tryLock()).thenReturn(true);

      scheduler.pollForDueJobs();

      verify(jobService).processReadyJobs();
      verify(lock).unlock();
    }

    @Test
    void whenLockNotAcquired_skipsProcessing() {
      when(lock.tryLock()).thenReturn(false);

      scheduler.pollForDueJobs();

      verify(jobService, never()).processReadyJobs();
      verify(lock, never()).unlock();
    }

    @Test
    void unlocksEvenWhenProcessingThrows() {
      when(lock.tryLock()).thenReturn(true);
      doThrow(new RuntimeException("redis down")).when(jobService).processReadyJobs();

      assertThatThrownBy(() -> scheduler.pollForDueJobs()).isInstanceOf(RuntimeException.class);
      verify(lock).unlock();
    }
  }
}
