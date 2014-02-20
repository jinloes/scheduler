package com.rivermeadow.scheduler.web;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.json.Json;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.web.JobController;

import org.apache.commons.io.IOUtils;
import org.cthul.matchers.CthulMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for {@link com.rivermeadow.api.web.JobController}.
 */
@ActiveProfiles("test")
@WebAppConfiguration
@SpringApplicationConfiguration(classes = ApplicationInitializer.class)
public class JobControllerTest extends AbstractTestNGSpringContextTests {
    private static final String UUID_REGEX =
            "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
    private static final Pattern JOBS_PATTERN = Pattern.compile("/api/v1/jobs/" + UUID_REGEX);
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @BeforeClass
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @DataProvider(name = "dataProvider")
    public Object[][] dataProvider() throws IOException {
        return new Object[][]{
                {
                        getRequest("post_job.json"), HttpMethod.POST,
                        Lists.newArrayList(Job.Status.RUNNING.toString(),
                                Job.Status.ERROR.toString())
                },
                // The first job will be running while the 2nd is queued up,
                // or may have already failed
                {
                        getRequest("put_job.json"), HttpMethod.PUT,
                        Lists.newArrayList(Job.Status.PENDING.toString(),
                                Job.Status.RUNNING.toString(), Job.Status.ERROR.toString())
                }
        };
    }

    private String getRequest(String filename) {
        try {
            return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(filename));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource.", e);
        }
    }

    @Test(dataProvider = "dataProvider")
    public void testScheduleJob(String requestBody, HttpMethod httpMethod,
            List<String> expectedStatus) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(JobController.ROOT_JOB_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", CustomMatchers.isUuid()))
                .andExpect(jsonPath("$.link",
                        CthulMatchers.matchesPattern(JOBS_PATTERN)))
                .andReturn();
        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        String jobsPath = String.format(JobController.JOB_LINK, "", id);
        mockMvc.perform(MockMvcRequestBuilders.get(jobsPath))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", CustomMatchers.isUuid()))
                .andExpect(jsonPath("$.status", isOneOf(expectedStatus.toArray())))
                .andExpect(jsonPath("$.task.uri",
                        equalTo("http://www.url.com")))
                .andExpect(jsonPath("$.task.method",
                        equalTo(httpMethod.toString())))
                .andExpect(jsonPath("$.task.response_code_ranges[0].start", equalTo(200)))
                .andExpect(jsonPath("$.task.response_code_ranges[0].end", equalTo(300)))
                .andExpect(jsonPath("$.task.body.user", equalTo("marco")))
                .andExpect(jsonPath("$.task.body.foo", equalTo("bar")));
    }

    @Test
    public void testScheduleJobValidationFailure() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(JobController.ROOT_JOB_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(Json.createObjectBuilder()
                        .add("schedule", "bad date")
                        .add("task", Json.createObjectBuilder()
                                .add("uri", "www.google.com")
                                .add("response_code_ranges", Json.createArrayBuilder()
                                        .add(Json.createObjectBuilder()
                                                .add("start", 200)
                                                .add("end", 300)
                                                .build()))
                                .build())
                        .build().toString()))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errors", hasItems(
                        CustomMatchers.isErrorField(ImmutableMap.of("field", "task.uri", "message",
                                "Invalid uri scheme. Supported")),
                        CustomMatchers.isErrorField(ImmutableMap.of("field", "schedule", "message",
                                "Invalid schedule date")))));
    }
}
