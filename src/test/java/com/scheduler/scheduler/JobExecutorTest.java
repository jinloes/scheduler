package com.scheduler.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scheduler.config.SchedulerProperties;
import com.scheduler.model.Job;
import com.scheduler.model.JobStatus;
import com.scheduler.model.ResponseCodeRange;
import com.scheduler.model.Task;
import com.scheduler.repository.JobRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class JobExecutorTest {

  @Mock private JobRepository jobRepository;
  @Mock private RestTemplate restTemplate;

  private SimpleMeterRegistry meterRegistry;
  private JobExecutor executor;

  @BeforeEach
  void setUp() {
    meterRegistry = new SimpleMeterRegistry();
    executor =
        new JobExecutor(
            jobRepository, restTemplate, new SchedulerProperties(5000, 1, 500), meterRegistry);
  }

  @Nested
  class ExecuteJob {

    @Test
    void success_marksJobSuccess() {
      UUID id = UUID.randomUUID();
      stubJob(id, task(0, 0L));
      stubHttp(200);

      executor.executeJob(id);

      verify(jobRepository).updateStatus(id, JobStatus.RUNNING);
      verify(jobRepository).updateStatus(id, JobStatus.SUCCESS);
    }

    @Test
    void alwaysFails_exhaustsRetriesAndMarksError() {
      UUID id = UUID.randomUUID();
      stubJob(id, task(2, 0L));
      stubHttp(500);

      executor.executeJob(id);

      // 1 initial + 2 retries = 3 HTTP calls
      verify(restTemplate, times(3))
          .exchange(anyString(), any(HttpMethod.class), any(), any(Class.class));
      verify(jobRepository).updateStatus(id, JobStatus.ERROR);
    }

    @Test
    void succeedsOnRetry_marksJobSuccess() {
      UUID id = UUID.randomUUID();
      stubJob(id, task(3, 0L));
      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), any(Class.class)))
          .thenReturn(ResponseEntity.status(500).body(""))
          .thenReturn(ResponseEntity.ok(""));

      executor.executeJob(id);

      verify(restTemplate, times(2))
          .exchange(anyString(), any(HttpMethod.class), any(), any(Class.class));
      verify(jobRepository).updateStatus(id, JobStatus.SUCCESS);
    }

    @Test
    void clientError_doesNotRetryAndMarksError() {
      UUID id = UUID.randomUUID();
      stubJob(id, task(3, 0L));
      stubHttp(404);

      executor.executeJob(id);

      // 4xx must not trigger retries
      verify(restTemplate, times(1))
          .exchange(anyString(), any(HttpMethod.class), any(), any(Class.class));
      verify(jobRepository).updateStatus(id, JobStatus.ERROR);
    }

    @Test
    void networkError_retriesAndMarksError() {
      UUID id = UUID.randomUUID();
      stubJob(id, task(2, 0L));
      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), any(Class.class)))
          .thenThrow(new ResourceAccessException("connection refused"));

      executor.executeJob(id);

      verify(restTemplate, times(3))
          .exchange(anyString(), any(HttpMethod.class), any(), any(Class.class));
      verify(jobRepository).updateStatus(id, JobStatus.ERROR);
    }

    @Test
    void clientErrorInCustomSuccessRange_treatedAsSuccess() {
      UUID id = UUID.randomUUID();
      Task task =
          new Task(
              "http://example.com/callback",
              "DELETE",
              null,
              List.of(new ResponseCodeRange(200, 299), new ResponseCodeRange(404, 404)),
              0,
              0L);
      stubJob(id, task);
      stubHttp(404);

      executor.executeJob(id);

      verify(jobRepository).updateStatus(id, JobStatus.SUCCESS);
    }

    @Test
    void jobNotFound_doesNothing() {
      UUID id = UUID.randomUUID();
      when(jobRepository.findById(id)).thenReturn(Optional.empty());

      executor.executeJob(id);

      verify(jobRepository, times(0)).updateStatus(any(), any());
    }

    @Test
    void noRetries_failsImmediately() {
      UUID id = UUID.randomUUID();
      stubJob(id, task(0, 0L));
      stubHttp(503);

      executor.executeJob(id);

      verify(restTemplate, times(1))
          .exchange(anyString(), any(HttpMethod.class), any(), any(Class.class));
      verify(jobRepository).updateStatus(id, JobStatus.ERROR);
    }

    @Test
    void recordsSuccessMetric() {
      UUID id = UUID.randomUUID();
      stubJob(id, task(0, 0L));
      stubHttp(200);

      executor.executeJob(id);

      assertThat(counter("scheduler.job.completed", "status", "success")).isEqualTo(1.0);
      assertThat(counter("scheduler.job.completed", "status", "error")).isZero();
    }

    @Test
    void recordsErrorMetricAndRetryCount() {
      UUID id = UUID.randomUUID();
      stubJob(id, task(2, 0L));
      stubHttp(500);

      executor.executeJob(id);

      assertThat(counter("scheduler.job.completed", "status", "error")).isEqualTo(1.0);
      assertThat(counter("scheduler.job.retries")).isEqualTo(2.0);
    }
  }

  private double counter(String name, String... tags) {
    Counter c = meterRegistry.find(name).tags(tags).counter();
    return c != null ? c.count() : 0.0;
  }

  private Task task(int maxRetries, long retryBackoffMs) {
    return new Task(
        "http://example.com/callback",
        "POST",
        null,
        List.of(new ResponseCodeRange(200, 299)),
        maxRetries,
        retryBackoffMs);
  }

  private void stubJob(UUID id, Task task) {
    Job job = new Job(id, task, "now", JobStatus.PENDING, Instant.now(), Instant.now());
    when(jobRepository.findById(id)).thenReturn(Optional.of(job));
  }

  private void stubHttp(int statusCode) {
    when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), any(Class.class)))
        .thenReturn(ResponseEntity.status(HttpStatus.valueOf(statusCode)).body(""));
  }
}
