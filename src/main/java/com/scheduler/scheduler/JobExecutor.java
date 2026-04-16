package com.scheduler.scheduler;

import com.scheduler.config.SchedulerProperties;
import com.scheduler.model.Job;
import com.scheduler.model.JobStatus;
import com.scheduler.model.ResponseCodeRange;
import com.scheduler.model.Task;
import com.scheduler.repository.JobRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * Consumes job IDs from the execution queue and performs the HTTP callback for each job. Runs a
 * fixed thread pool that blocks on the queue using BLPOP semantics.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JobExecutor {

  private static final Set<HttpMethod> BODY_METHODS =
      Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);

  private final JobRepository jobRepository;
  private final RestTemplate restTemplate;
  private final SchedulerProperties schedulerProperties;

  private ExecutorService executorService;
  private final AtomicBoolean running = new AtomicBoolean(false);

  /** Starts the executor thread pool. Called automatically by Spring after bean construction. */
  @PostConstruct
  public void start() {
    running.set(true);
    executorService = Executors.newFixedThreadPool(schedulerProperties.executionThreads());
    for (int i = 0; i < schedulerProperties.executionThreads(); i++) {
      executorService.submit(this::consumeLoop);
    }
    log.info("JobExecutor started with {} threads", schedulerProperties.executionThreads());
  }

  /** Signals threads to stop and waits up to 10 seconds for a clean shutdown. */
  @PreDestroy
  public void stop() {
    running.set(false);
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
        log.warn("Executor threads did not terminate cleanly, forcing shutdown");
        executorService.shutdownNow();
      }
    } catch (InterruptedException e) {
      log.warn("Interrupted while awaiting executor shutdown, forcing shutdown");
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  private void consumeLoop() {
    while (running.get()) {
      try {
        String jobId = jobRepository.pollExecutionQueue(1);
        if (jobId != null) {
          executeJob(UUID.fromString(jobId));
        }
      } catch (IllegalArgumentException e) {
        log.error("Malformed job ID dequeued from execution queue", e);
      } catch (Exception e) {
        log.error("Unexpected error in executor loop", e);
      }
    }
  }

  private void executeJob(UUID jobId) {
    Optional<Job> jobOpt = jobRepository.findById(jobId);
    if (jobOpt.isEmpty()) {
      log.warn("Job {} not found for execution", jobId);
      return;
    }
    Job job = jobOpt.get();
    jobRepository.updateStatus(jobId, JobStatus.RUNNING);
    log.info("Executing job {}: {} {}", jobId, job.getTask().method(), job.getTask().uri());

    try {
      int statusCode = callTarget(job.getTask());
      JobStatus result = evaluateResponse(statusCode, job.getTask().responseCodeRanges());
      jobRepository.updateStatus(jobId, result);
      log.info("Job {} completed with status {} (HTTP {})", jobId, result, statusCode);
    } catch (Exception e) {
      log.error("Job {} failed", jobId, e);
      jobRepository.updateStatus(jobId, JobStatus.ERROR);
    }
  }

  private int callTarget(Task task) {
    HttpMethod method = HttpMethod.valueOf(task.method());
    HttpHeaders headers = new HttpHeaders();
    Object body = null;
    if (BODY_METHODS.contains(method)) {
      headers.setContentType(MediaType.APPLICATION_JSON);
      body = task.body();
    }
    HttpEntity<Object> entity = new HttpEntity<>(body, headers);
    try {
      ResponseEntity<String> response =
          restTemplate.exchange(task.uri(), method, entity, String.class);
      return response.getStatusCode().value();
    } catch (HttpStatusCodeException e) {
      return e.getStatusCode().value();
    }
  }

  private JobStatus evaluateResponse(int statusCode, List<ResponseCodeRange> ranges) {
    // No ranges configured — fall back to standard 2xx success convention.
    if (ranges == null || ranges.isEmpty()) {
      return statusCode >= 200 && statusCode < 300 ? JobStatus.SUCCESS : JobStatus.ERROR;
    }
    return ranges.stream().anyMatch(r -> r.contains(statusCode))
        ? JobStatus.SUCCESS
        : JobStatus.ERROR;
  }
}
