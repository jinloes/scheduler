package com.rivermeadow.scheduler.web;

import java.io.IOException;
import java.util.regex.Pattern;

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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;

/**
 * Tests for {@link com.rivermeadow.api.web.JobController}.
 */
@ActiveProfiles("test")
@WebAppConfiguration
@SpringApplicationConfiguration(classes = ApplicationInitializer.class)
public class JobControllerTest extends AbstractTestNGSpringContextTests {
    private static final String UUID_REGEX =
            "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
    private static final Pattern UUID_PATTERN = Pattern.compile("^" + UUID_REGEX);
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
        return new Object[][] {
                { getResource("post_job.json"), HttpMethod.POST, Job.Status.RUNNING },
                // The first job will be running while the 2nd is queued up
                { getResource("put_job.json"), HttpMethod.PUT, Job.Status.PENDING }
        };
    }
    private String getResource(String filename) {
        try {
            return IOUtils.toString(getClass().getClassLoader().getResourceAsStream(filename));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource.", e);
        }
    }

    @Test(dataProvider = "dataProvider")
    public void testScheduleJob(String requestBody, HttpMethod httpMethod,
                                Job.Status jobStatus) throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(JobController.ROOT_JOB_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
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
                        equalTo(jobStatus.toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.task.uri",
                        equalTo("http://www.url.com")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.task.method",
                        equalTo(httpMethod.toString())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.task.expected_range",
                        equalTo("200-300")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.task.body.user", equalTo("marco")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.task.body.foo", equalTo("bar")));
    }
}
