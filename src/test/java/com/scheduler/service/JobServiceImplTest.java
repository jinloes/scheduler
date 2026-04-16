package com.scheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scheduler.config.SchedulerProperties;
import com.scheduler.model.Job;
import com.scheduler.model.Task;
import com.scheduler.repository.JobRepository;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

  @Mock private JobRepository jobRepository;

  private JobServiceImpl jobService;

  @BeforeEach
  void setUp() {
    jobService = new JobServiceImpl(jobRepository, new SchedulerProperties(5000, 4, 500));
  }

  @Nested
  class Schedule {

    @Test
    void savesJobAndReturnsIt() {
      Task task = new Task("http://example.com", "GET", null, null, null, null);

      Job job = jobService.schedule(task, "now");

      assertThat(job.getId()).isNotNull();
      assertThat(job.getTask()).isEqualTo(task);
      verify(jobRepository).save(job);
    }
  }

  @Nested
  class GetById {

    @Test
    void delegatesToRepository() {
      UUID id = UUID.randomUUID();
      when(jobRepository.findById(id)).thenReturn(Optional.empty());

      Optional<Job> result = jobService.getById(id);

      assertThat(result).isEmpty();
      verify(jobRepository).findById(id);
    }
  }

  @Nested
  class ProcessReadyJobs {

    @Test
    void queuesEachDueJob() {
      String id1 = UUID.randomUUID().toString();
      String id2 = UUID.randomUUID().toString();
      when(jobRepository.getDueJobIds(anyLong(), eq(500))).thenReturn(Set.of(id1, id2));

      jobService.processReadyJobs();

      verify(jobRepository).atomicQueueForExecution(id1);
      verify(jobRepository).atomicQueueForExecution(id2);
    }

    @Test
    void passesBatchSizeToRepository() {
      when(jobRepository.getDueJobIds(anyLong(), anyInt())).thenReturn(Set.of());

      jobService.processReadyJobs();

      ArgumentCaptor<Integer> limitCaptor = ArgumentCaptor.forClass(Integer.class);
      verify(jobRepository).getDueJobIds(anyLong(), limitCaptor.capture());
      assertThat(limitCaptor.getValue()).isEqualTo(500);
    }

    @Test
    void skipsMalformedJobIdAndContinues() {
      String goodId = UUID.randomUUID().toString();
      String badId = "not-a-valid-id";
      when(jobRepository.getDueJobIds(anyLong(), anyInt())).thenReturn(Set.of(goodId, badId));
      lenient()
          .doThrow(new IllegalArgumentException("bad id"))
          .when(jobRepository)
          .atomicQueueForExecution(badId);

      jobService.processReadyJobs();

      verify(jobRepository).atomicQueueForExecution(goodId);
    }

    @Test
    void doesNothingWhenNoDueJobs() {
      when(jobRepository.getDueJobIds(anyLong(), anyInt())).thenReturn(Set.of());

      jobService.processReadyJobs();

      verify(jobRepository, never()).atomicQueueForExecution(anyString());
    }
  }
}
