package com.scheduler.model;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/** Represents a scheduled HTTP callback job and its current execution state. */
@Getter
public class Job {

  private final UUID id;
  private final Task task;
  private final String schedule;
  private final Instant scheduledAt;
  private final Instant createdAt;
  @Setter private JobStatus status;

  /**
   * Creates a new job with a generated ID and PENDING status.
   *
   * @param schedule ISO-8601 datetime string or {@code "now"} for immediate execution
   * @throws IllegalArgumentException if {@code schedule} is null or unparseable
   */
  public Job(Task task, String schedule) {
    this.id = UUID.randomUUID();
    this.task = task;
    this.schedule = schedule;
    this.status = JobStatus.PENDING;
    this.createdAt = Instant.now();
    this.scheduledAt = parseSchedule(schedule);
  }

  /** Reconstructs a job from stored fields (e.g. deserialized from Redis). */
  public Job(
      UUID id,
      Task task,
      String schedule,
      JobStatus status,
      Instant scheduledAt,
      Instant createdAt) {
    this.id = id;
    this.task = task;
    this.schedule = schedule;
    this.status = status;
    this.scheduledAt = scheduledAt;
    this.createdAt = createdAt;
  }

  private static Instant parseSchedule(String schedule) {
    if (schedule == null) {
      throw new IllegalArgumentException("schedule is required");
    }
    if ("now".equalsIgnoreCase(schedule)) {
      return Instant.now();
    }
    try {
      return Instant.parse(schedule);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid schedule format: use ISO-8601 (e.g. '2024-12-01T10:00:00Z') or 'now'");
    }
  }
}
