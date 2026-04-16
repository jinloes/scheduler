package com.scheduler.web;

import com.scheduler.model.Job;
import com.scheduler.service.JobService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for scheduling and retrieving jobs. */
@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

  private final JobService jobService;

  /**
   * Schedules a new job.
   *
   * @return 201 Created with the job ID and a link to its status endpoint
   */
  @PostMapping
  public ResponseEntity<JobCreatedResponse> scheduleJob(@RequestBody @Valid JobRequest request) {
    Job saved = jobService.schedule(request.task(), request.schedule());
    String id = saved.getId().toString();
    String link = "/api/v1/jobs/" + id;
    return ResponseEntity.created(URI.create(link)).body(new JobCreatedResponse(id, link));
  }

  /**
   * Returns the current state of a job.
   *
   * @return 200 with the job, or 404 if not found
   */
  @GetMapping("/{jobId}")
  public ResponseEntity<Job> getJob(@PathVariable UUID jobId) {
    return jobService
        .getById(jobId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
