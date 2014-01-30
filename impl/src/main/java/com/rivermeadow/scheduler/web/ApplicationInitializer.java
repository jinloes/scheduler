package com.rivermeadow.scheduler.web;

import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.rivermeadow.api.model.Job;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.queue.DistributedDelayQueue;
import org.apache.curator.framework.recipes.queue.QueueBuilder;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.recipes.queue.QueueSerializer;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Application configuration and bean definitions.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.rivermeadow.scheduler"})
public class ApplicationInitializer extends SpringBootServletInitializer {
    private static final String SCHEDULER_NAMESPACE = "scheduler";
    private static final String APPLICATION_ROOT_PATH = "/api/v1/*";

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ApplicationInitializer.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ApplicationInitializer.class);
    }

    @Bean
    public ServletRegistrationBean dispatcherRegistration(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
        registration.addUrlMappings(APPLICATION_ROOT_PATH);
        return registration;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JodaModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    @Bean
    @Autowired
    public CuratorFramework getCurator(@Qualifier("connectString") final String connectString,
            @Value("#{systemProperties['zookeeper.wait_time_ms']?: 100}") final long waitTime,
            @Value("#{systemProperties['zookeeper.sleep_between_retries_ms']?: 5}") final long
                    sleepBetweenRetries) {
        CuratorFramework curator = CuratorFrameworkFactory.builder()
                .retryPolicy(new RetryUntilElapsed(
                        (int) TimeUnit.SECONDS.toMillis(waitTime),
                        (int) TimeUnit.SECONDS.toMillis(sleepBetweenRetries)))
                .connectString(connectString)
                .namespace(SCHEDULER_NAMESPACE)
                .build();
        curator.start();
        return curator;
    }

    @Bean
    @Autowired
    public DistributedDelayQueue<Job> getJobQueue(final CuratorFramework client, final QueueConsumer<Job> jobConsumer,
                                             final QueueSerializer<Job> serializer) {
        QueueBuilder<Job> builder = QueueBuilder.builder(client, jobConsumer, serializer, "/jobs");
        DistributedDelayQueue<Job> jobQueue = builder.lockPath("/locks").buildDelayQueue();
        try {
            jobQueue.start();
        } catch (Exception e) {
            throw new RuntimeException("Could not create job queue.");
        }
        return jobQueue;
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Configuration
    @Profile("test")
    public static class TestApplication {
        @Bean
        public String connectString() throws Exception{
            TestingServer testingServer = new TestingServer(InstanceSpec.newInstanceSpec());
            return testingServer.getConnectString();
        }
    }

    @Configuration
    @Profile("default")
    public static class DefaultApplication {
        @Bean
        public String connectString(
                @Value("#{systemProperties['zookeeper.connect_url']?:'localhost:2181'}") final String connectString) {
            return connectString;
        }
    }
}
