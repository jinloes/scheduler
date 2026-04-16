package com.scheduler.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JobTest {

  @Nested
  class NewJobConstructor {

    @Test
    void assignsGeneratedIdAndPendingStatus() {
      Task task = task();
      Job job = new Job(task, "now");

      assertThat(job.getId()).isNotNull();
      assertThat(job.getStatus()).isEqualTo(JobStatus.PENDING);
      assertThat(job.getTask()).isEqualTo(task);
      assertThat(job.getSchedule()).isEqualTo("now");
      assertThat(job.getCreatedAt()).isNotNull();
    }

    @Test
    void setsScheduledAtToNow_forNowSchedule() {
      long before = System.currentTimeMillis();
      Job job = new Job(task(), "now");
      long after = System.currentTimeMillis();

      assertThat(job.getScheduledAt().toEpochMilli()).isBetween(before, after);
    }

    @Test
    void parsesIso8601Schedule() {
      Job job = new Job(task(), "2030-06-15T12:00:00Z");

      assertThat(job.getScheduledAt().toString()).isEqualTo("2030-06-15T12:00:00Z");
    }

    @Test
    void throwsForNullSchedule() {
      assertThatThrownBy(() -> new Job(task(), null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("schedule");
    }

    @Test
    void throwsForUnparseableSchedule() {
      assertThatThrownBy(() -> new Job(task(), "next tuesday"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("ISO-8601");
    }
  }

  @Nested
  class ReconstructionConstructor {

    @Test
    void populatesAllFields() {
      var id = java.util.UUID.randomUUID();
      var task = task();
      var now = java.time.Instant.now();

      Job job = new Job(id, task, "now", JobStatus.RUNNING, now, now);

      assertThat(job.getId()).isEqualTo(id);
      assertThat(job.getTask()).isEqualTo(task);
      assertThat(job.getStatus()).isEqualTo(JobStatus.RUNNING);
      assertThat(job.getScheduledAt()).isEqualTo(now);
      assertThat(job.getCreatedAt()).isEqualTo(now);
    }
  }

  private Task task() {
    return new Task("http://example.com", "GET", null, null, null, null);
  }
}
