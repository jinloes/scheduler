package com.scheduler.service;

import com.scheduler.config.SchedulerProperties;
import com.scheduler.model.Job;
import com.scheduler.model.Task;
import com.scheduler.repository.JobRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

  private final JobRepository jobRepository;
  private final SchedulerProperties schedulerProperties;

  @Override
  public Job schedule(Task task, String schedule) {
    Job job = new Job(task, schedule);
    jobRepository.save(job);
    log.info("Scheduled job {} for {}", job.getId(), job.getScheduledAt());
    return job;
  }

  @Override
  public Optional<Job> getById(UUID id) {
    return jobRepository.findById(id);
  }

  @Override
  public void processReadyJobs() {
    Set<String> dueJobIds =
        jobRepository.getDueJobIds(
            Instant.now().toEpochMilli(), schedulerProperties.pollBatchSize());
    for (String jobId : dueJobIds) {
      try {
        jobRepository.atomicQueueForExecution(jobId);
        log.debug("Queued job {} for execution", jobId);
      } catch (IllegalArgumentException e) {
        log.error("Skipping malformed job ID in pending set: {}", jobId);
      }
    }
    if (!dueJobIds.isEmpty()) {
      log.info("Queued {} jobs for execution", dueJobIds.size());
    }
  }
}
