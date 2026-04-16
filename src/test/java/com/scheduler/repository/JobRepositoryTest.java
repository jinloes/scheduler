package com.scheduler.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scheduler.model.Job;
import com.scheduler.model.JobStatus;
import com.scheduler.model.Task;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class JobRepositoryTest {

  @Mock private RedisTemplate<String, String> redisTemplate;
  @Mock private HashOperations<String, Object, Object> hashOps;
  @Mock private ZSetOperations<String, String> zSetOps;
  @Mock private ListOperations<String, String> listOps;

  private JobRepository repository;

  @BeforeEach
  void setUp() {
    lenient().when(redisTemplate.opsForHash()).thenReturn(hashOps);
    lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
    lenient().when(redisTemplate.opsForList()).thenReturn(listOps);
    repository =
        new JobRepository(
            redisTemplate,
            JsonMapper.builder().disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS).build());
  }

  @Nested
  class Save {

    @Test
    void persistsHashAndAddsToSortedSet() {
      Job job = new Job(task(), "now");

      repository.save(job);

      verify(hashOps).putAll(eq("job:" + job.getId()), anyMap());
      verify(zSetOps).add(eq("jobs:pending"), eq(job.getId().toString()), anyDouble());
    }
  }

  @Nested
  class FindById {

    @Test
    void returnsEmpty_whenHashMissing() {
      UUID id = UUID.randomUUID();
      when(hashOps.entries("job:" + id)).thenReturn(Map.of());

      assertThat(repository.findById(id)).isEmpty();
    }

    @Test
    void deserializesJob_whenHashPresent() {
      UUID id = UUID.randomUUID();
      Instant now = Instant.ofEpochMilli(Instant.now().toEpochMilli());
      Map<Object, Object> raw = new HashMap<>();
      raw.put("id", id.toString());
      raw.put("task", "{\"uri\":\"http://example.com\",\"method\":\"POST\"}");
      raw.put("schedule", "now");
      raw.put("status", "RUNNING");
      raw.put("scheduledAt", String.valueOf(now.toEpochMilli()));
      raw.put("createdAt", String.valueOf(now.toEpochMilli()));
      when(hashOps.entries("job:" + id)).thenReturn(raw);

      Optional<Job> result = repository.findById(id);

      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(id);
      assertThat(result.get().getStatus()).isEqualTo(JobStatus.RUNNING);
      assertThat(result.get().getTask().uri()).isEqualTo("http://example.com");
    }
  }

  @Nested
  class UpdateStatus {

    @Test
    void setsStatusField() {
      UUID id = UUID.randomUUID();

      repository.updateStatus(id, JobStatus.RUNNING);

      verify(hashOps).put("job:" + id, "status", "RUNNING");
    }

    @Test
    void removesFromSortedSet_forNonPendingStatus() {
      UUID id = UUID.randomUUID();

      repository.updateStatus(id, JobStatus.RUNNING);

      verify(zSetOps).remove("jobs:pending", id.toString());
    }

    @Test
    void doesNotRemoveFromSortedSet_forPendingStatus() {
      UUID id = UUID.randomUUID();

      repository.updateStatus(id, JobStatus.PENDING);

      verify(zSetOps, never()).remove(anyString(), any());
    }

    @Test
    void setsTtl_forSuccessAndError() {
      UUID id = UUID.randomUUID();

      repository.updateStatus(id, JobStatus.SUCCESS);
      repository.updateStatus(id, JobStatus.ERROR);

      verify(redisTemplate, times(2)).expire(anyString(), any(Duration.class));
    }

    @Test
    void doesNotSetTtl_forNonTerminalStatus() {
      UUID id = UUID.randomUUID();

      repository.updateStatus(id, JobStatus.RUNNING);

      verify(redisTemplate, never()).expire(anyString(), any(Duration.class));
    }
  }

  @Nested
  class GetDueJobIds {

    @Test
    void queriesWithCorrectScoreAndLimit() {
      when(zSetOps.rangeByScore(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong()))
          .thenReturn(Set.of());

      repository.getDueJobIds(1000L, 50);

      verify(zSetOps).rangeByScore("jobs:pending", 0, 1000L, 0, 50);
    }
  }

  @Nested
  class AtomicQueueForExecution {

    @Test
    void executesLuaScriptWithCorrectKeysAndArgs() {
      String jobId = UUID.randomUUID().toString();

      repository.atomicQueueForExecution(jobId);

      verify(redisTemplate)
          .execute(
              any(RedisScript.class),
              eq(List.of("job:" + jobId, "jobs:pending", "jobs:execution")),
              eq("QUEUED"),
              eq(jobId));
    }
  }

  @Nested
  class PollExecutionQueue {

    @Test
    void returnsJobId() {
      String jobId = UUID.randomUUID().toString();
      when(listOps.rightPop("jobs:execution", Duration.ofSeconds(3))).thenReturn(jobId);

      assertThat(repository.pollExecutionQueue(3)).isEqualTo(jobId);
    }

    @Test
    void returnsNull_onTimeout() {
      when(listOps.rightPop(anyString(), any(Duration.class))).thenReturn(null);

      assertThat(repository.pollExecutionQueue(1)).isNull();
    }
  }

  private Task task() {
    return new Task("http://example.com", "POST", null, null, null, null);
  }
}
