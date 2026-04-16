package com.scheduler.service;

import com.scheduler.model.Job;
import com.scheduler.model.Task;
import java.util.Optional;
import java.util.UUID;

/** Manages job lifecycle: scheduling, retrieval, and queuing due jobs for execution. */
public interface JobService {

  /**
   * Persists a new job and adds it to the pending queue.
   *
   * @param task the HTTP callback to execute
   * @param schedule ISO-8601 datetime string or {@code "now"}
   * @throws IllegalArgumentException if {@code schedule} is null or unparseable
   */
  Job schedule(Task task, String schedule);

  /** Returns the job with the given ID, or empty if not found. */
  Optional<Job> getById(UUID id);

  /** Moves all due jobs from the pending sorted set into the execution queue. */
  void processReadyJobs();
}
