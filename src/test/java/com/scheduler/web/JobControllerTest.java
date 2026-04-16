package com.scheduler.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.scheduler.model.Job;
import com.scheduler.model.JobStatus;
import com.scheduler.model.ResponseCodeRange;
import com.scheduler.model.Task;
import com.scheduler.service.JobService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = {JobController.class, GlobalExceptionHandler.class})
class JobControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockitoBean private JobService jobService;

  @Nested
  class ScheduleJob {

    @Test
    void returnsCreated() throws Exception {
      Task task =
          new Task(
              "http://example.com/callback",
              "POST",
              Map.of("key", "value"),
              List.of(new ResponseCodeRange(200, 299)),
              null,
              null);
      Job job = new Job(task, "now");
      when(jobService.schedule(any(Task.class), anyString())).thenReturn(job);

      String body = objectMapper.writeValueAsString(new JobRequest(task, "now"));
      mockMvc
          .perform(post("/api/v1/jobs").contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").isNotEmpty())
          .andExpect(jsonPath("$.link").isNotEmpty());
    }

    @Test
    void missingTask_returnsBadRequest() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/jobs")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"schedule\":\"now\"}"))
          .andExpect(status().isBadRequest());
    }

    @Test
    void invalidTaskUri_returnsBadRequest() throws Exception {
      String body = "{\"task\":{\"uri\":\"not-a-url\",\"method\":\"POST\"},\"schedule\":\"now\"}";
      mockMvc
          .perform(post("/api/v1/jobs").contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isBadRequest());
    }

    @Test
    void invalidMethod_returnsBadRequest() throws Exception {
      String body =
          "{\"task\":{\"uri\":\"http://example.com\",\"method\":\"INVALID\"},\"schedule\":\"now\"}";
      mockMvc
          .perform(post("/api/v1/jobs").contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isBadRequest());
    }

    @Test
    void maxRetriesAboveLimit_returnsBadRequest() throws Exception {
      String body =
          "{\"task\":{\"uri\":\"http://example.com\",\"method\":\"GET\",\"max_retries\":99}"
              + ",\"schedule\":\"now\"}";
      mockMvc
          .perform(post("/api/v1/jobs").contentType(MediaType.APPLICATION_JSON).content(body))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  class GetJob {

    @Test
    void found_returnsOk() throws Exception {
      UUID id = UUID.randomUUID();
      Task task = new Task("http://example.com/callback", "POST", null, null, null, null);
      Instant now = Instant.now();
      Job job = new Job(id, task, "now", JobStatus.PENDING, now, now);
      when(jobService.getById(id)).thenReturn(Optional.of(job));

      mockMvc.perform(get("/api/v1/jobs/" + id)).andExpect(status().isOk());
    }

    @Test
    void notFound_returns404() throws Exception {
      UUID id = UUID.randomUUID();
      when(jobService.getById(id)).thenReturn(Optional.empty());

      mockMvc.perform(get("/api/v1/jobs/" + id)).andExpect(status().isNotFound());
    }
  }
}
