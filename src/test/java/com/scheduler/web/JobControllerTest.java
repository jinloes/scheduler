package com.scheduler.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {JobController.class, GlobalExceptionHandler.class})
class JobControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private JobService jobService;

  @Test
  void scheduleJob_returnsCreated() throws Exception {
    Task task =
        new Task(
            "http://example.com/callback",
            "POST",
            Map.of("key", "value"),
            List.of(new ResponseCodeRange(200, 299)));
    Job job = new Job(task, "now");
    when(jobService.schedule(any(Task.class), anyString())).thenReturn(job);

    JobRequest request = new JobRequest(task, "now");
    String body = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(post("/api/v1/jobs").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.link").isNotEmpty());
  }

  @Test
  void scheduleJob_missingTask_returnsBadRequest() throws Exception {
    String body = "{\"schedule\":\"now\"}";

    mockMvc
        .perform(post("/api/v1/jobs").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getJob_found_returnsOk() throws Exception {
    UUID id = UUID.randomUUID();
    Task task = new Task("http://example.com/callback", "POST", null, null);
    Instant now = Instant.now();
    Job job = new Job(id, task, "now", JobStatus.PENDING, now, now);
    when(jobService.getById(id)).thenReturn(Optional.of(job));

    mockMvc.perform(get("/api/v1/jobs/" + id)).andExpect(status().isOk());
  }

  @Test
  void getJob_notFound_returns404() throws Exception {
    UUID id = UUID.randomUUID();
    when(jobService.getById(id)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/v1/jobs/" + id)).andExpect(status().isNotFound());
  }
}
