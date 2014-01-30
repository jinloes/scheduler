package com.rivermeadow.scheduler.web;

import java.util.Map;
import java.util.regex.Pattern;

import com.jayway.jsonpath.JsonPath;
import com.rivermeadow.api.model.Job;
import com.rivermeadow.api.web.JobController;

import org.apache.commons.io.IOUtils;
import org.cthul.matchers.CthulMatchers;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link com.rivermeadow.api.web.JobController}.
 */
@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringApplicationConfiguration(classes = ApplicationInitializer.class)
public class JobControllerTest {
    private static final String UUID_REGEX =
            "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
    private static final Pattern UUID_PATTERN = Pattern.compile("^" + UUID_REGEX);
    private static final Pattern JOBS_PATTERN = Pattern.compile("/api/v1/jobs/" + UUID_REGEX);
    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void testScheduleJob() throws Exception {
        String json = IOUtils.toString(getClass().getClassLoader()
                .getResourceAsStream("post_job.json"));
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(JobController.ROOT_JOB_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id",
                        CthulMatchers.matchesPattern((UUID_PATTERN))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.link",
                        CthulMatchers.matchesPattern(JOBS_PATTERN)))
                .andReturn();
        String id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        String jobsPath = String.format(JobController.JOB_LINK, "", id);
        mockMvc.perform(MockMvcRequestBuilders.get(jobsPath))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", not(isEmptyOrNullString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",
                        equalTo(Job.Status.RUNNING.toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.task.uri",
                        equalTo("http://www.url.com")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.task.method", equalTo("POST")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.task.expected_range",
                        equalTo("200-300")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.task.body.user", equalTo("marco")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.task.body.foo", equalTo("bar")));
    }
}
