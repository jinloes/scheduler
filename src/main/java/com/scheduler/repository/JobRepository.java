package com.scheduler.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scheduler.model.Job;
import com.scheduler.model.JobStatus;
import com.scheduler.model.Task;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

/** Redis-backed store for job state, the pending sorted set, and the execution queue. */
@Repository
@RequiredArgsConstructor
public class JobRepository {

  private static final String JOB_KEY_PREFIX = "job:";
  private static final String PENDING_SORTED_SET = "jobs:pending";
  private static final String EXECUTION_QUEUE = "jobs:execution";
  private static final Duration TERMINAL_JOB_TTL = Duration.ofDays(7);

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  /** Persists the job hash and adds it to the pending sorted set scored by scheduled time. */
  public void save(Job job) {
    String key = JOB_KEY_PREFIX + job.getId();
    try {
      JobHash hash =
          new JobHash(
              job.getId().toString(),
              objectMapper.writeValueAsString(job.getTask()),
              job.getSchedule(),
              job.getStatus().name(),
              String.valueOf(job.getScheduledAt().toEpochMilli()),
              String.valueOf(job.getCreatedAt().toEpochMilli()));
      redisTemplate.opsForHash().putAll(key, objectMapper.convertValue(hash, Map.class));
      redisTemplate
          .opsForZSet()
          .add(PENDING_SORTED_SET, job.getId().toString(), job.getScheduledAt().toEpochMilli());
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize job " + job.getId(), e);
    }
  }

  /** Returns the job with the given ID, or empty if no hash exists in Redis for it. */
  public Optional<Job> findById(UUID id) {
    Map<Object, Object> raw = redisTemplate.opsForHash().entries(JOB_KEY_PREFIX + id);
    if (raw.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(deserialize(objectMapper.convertValue(raw, JobHash.class)));
  }

  /**
   * Updates the job's status field in Redis. Removes the job from the pending sorted set for any
   * non-PENDING status, and sets a TTL on terminal states (SUCCESS, ERROR).
   */
  public void updateStatus(UUID id, JobStatus status) {
    String key = JOB_KEY_PREFIX + id;
    redisTemplate.opsForHash().put(key, "status", status.name());
    if (status != JobStatus.PENDING) {
      redisTemplate.opsForZSet().remove(PENDING_SORTED_SET, id.toString());
    }
    if (status == JobStatus.SUCCESS || status == JobStatus.ERROR) {
      redisTemplate.expire(key, TERMINAL_JOB_TTL);
    }
  }

  /**
   * Returns the IDs of all jobs in the pending sorted set with a scheduled time at or before {@code
   * maxScoreEpochMs}.
   */
  public Set<String> getDueJobIds(long maxScoreEpochMs) {
    return redisTemplate.opsForZSet().rangeByScore(PENDING_SORTED_SET, 0, maxScoreEpochMs);
  }

  /** Pushes a job ID onto the left end of the execution queue. */
  public void enqueueForExecution(String jobId) {
    redisTemplate.opsForList().leftPush(EXECUTION_QUEUE, jobId);
  }

  /**
   * Blocks up to {@code timeoutSeconds} waiting for a job ID from the right end of the execution
   * queue. Returns {@code null} if the timeout elapses with no item available.
   */
  public String pollExecutionQueue(long timeoutSeconds) {
    return redisTemplate.opsForList().rightPop(EXECUTION_QUEUE, Duration.ofSeconds(timeoutSeconds));
  }

  private Job deserialize(JobHash hash) {
    try {
      UUID id = UUID.fromString(Objects.requireNonNull(hash.id(), "missing field: id"));
      Task task =
          objectMapper.readValue(
              Objects.requireNonNull(hash.task(), "missing field: task"), Task.class);
      String schedule = Objects.requireNonNull(hash.schedule(), "missing field: schedule");
      JobStatus status =
          JobStatus.valueOf(Objects.requireNonNull(hash.status(), "missing field: status"));
      Instant scheduledAt =
          Instant.ofEpochMilli(
              Long.parseLong(
                  Objects.requireNonNull(hash.scheduledAt(), "missing field: scheduledAt")));
      Instant createdAt =
          Instant.ofEpochMilli(
              Long.parseLong(Objects.requireNonNull(hash.createdAt(), "missing field: createdAt")));
      return new Job(id, task, schedule, status, scheduledAt, createdAt);
    } catch (IOException e) {
      throw new RuntimeException("Failed to deserialize job", e);
    }
  }

  private record JobHash(
      String id,
      String task,
      String schedule,
      String status,
      String scheduledAt,
      String createdAt) {}
}
