package com.rivermeadow.scheduler.web;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.rivermeadow.scheduler.util.QueueJobsTask;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.curator.test.InstanceSpec;
import org.apache.curator.test.TestingServer;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.DefaultManagedTaskScheduler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Application configuration and bean definitions.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.rivermeadow.scheduler"})
public class ApplicationInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);
    private static final String SCHEDULER_NAMESPACE = "scheduler";
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");
    private static final String QUEUE_WATCHER_PATH = "/queue-watcher";
    public static final String APPLICATION_ROOT_PATH = "/api/v1";
    @Value("#{systemProperties['cassandra.poll.rate']?: 15}")
    private int queueTaskRateSecs;

    @Autowired
    private CuratorFramework curator;
    @Autowired
    private QueueJobsTask queueJobsTask;

    static {
        DateTimeZone.setDefault(DateTimeZone.UTC);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ApplicationInitializer.class, args);
    }

    @PostConstruct
    public void afterContextCreated() {
        LeaderSelector leaderSelector = new LeaderSelector(curator, QUEUE_WATCHER_PATH,
                new LeaderSelectorListenerAdapter() {
                    @Override
                    public void takeLeadership(CuratorFramework client) throws Exception {
                        logger.debug("Taking leadership for job queueing.");
                        DefaultManagedTaskScheduler scheduler = new DefaultManagedTaskScheduler();
                        scheduler.scheduleAtFixedRate(queueJobsTask,
                                TimeUnit.SECONDS.toMillis(queueTaskRateSecs));
                    }
                });
        leaderSelector.start();
    }

    @Bean
    public String apiUsername(
            @Value("#{systemProperties['scheduler.api.username']?: " +
                    "'scheduler-user@rivermeadow.com'}")
            String username) {
        return username;
    }

    @Bean
    public String apiPassword(
            @Value("#{systemProperties['scheduler.api.password']?: 'secret'}") String password) {
        return password;
    }

    @Bean
    public ServletRegistrationBean dispatcherRegistration(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
        registration.addUrlMappings(APPLICATION_ROOT_PATH + "/*");
        return registration;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JodaModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Bean
    public Session keyspace(
            @Value("#{systemProperties['cassandra.hosts']?: '127.0.0.1'}") String connectUrl,
            @Value("#{systemProperties['cassandra.port']?: 9042}") int port,
            @Value("#{systemProperties['cassandra.keyspace']?: 'scheduler'}") String keyspace) {
        return Cluster.builder()
                .withPort(port)
                .addContactPoints(COMMA_PATTERN.split(connectUrl))
                .build()
                .connect(keyspace);
    }

    @Bean
    @Autowired
    public CuratorFramework curator(@Qualifier("connectString") final String connectString,
            @Value("#{systemProperties['zookeeper.wait_time_ms']?: 100}") final long waitTime,
            @Value("#{systemProperties['zookeeper.sleep_between_retries_ms']?: 5}")
            final long sleepBetweenRetries) {
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
    public RestTemplate restTemplate() {
        return new RestTemplate(
                new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault()));
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Configuration
    @Profile("test")
    public static class TestApplication {
        @Bean
        public String connectString() throws Exception {
            TestingServer testingServer = new TestingServer(InstanceSpec.newInstanceSpec());
            return testingServer.getConnectString();
        }
    }

    @Configuration
    @Profile("default")
    public static class DefaultApplication {
        @Bean
        public String connectString(
                @Value("#{systemProperties['zookeeper.connect_url']?:'localhost:2181'}")
                final String connectString) {
            return connectString;
        }
    }
}
