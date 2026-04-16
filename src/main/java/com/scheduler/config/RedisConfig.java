package com.scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/** Spring bean configuration for Redis, HTTP client, and JSON serialization. */
@Configuration
public class RedisConfig {

  /** Configures all keys and values as plain strings to keep Redis data human-readable. */
  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    StringRedisSerializer serializer = new StringRedisSerializer();
    template.setKeySerializer(serializer);
    template.setValueSerializer(serializer);
    template.setHashKeySerializer(serializer);
    template.setHashValueSerializer(serializer);
    return template;
  }

  @Bean
  public LockRegistry redisLockRegistry(RedisConnectionFactory connectionFactory) {
    return new RedisLockRegistry(connectionFactory, "scheduler:locks", 30_000L);
  }

  @Bean
  public ObjectMapper objectMapper() {
    return JsonMapper.builder()
        .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
        // Match Spring Boot's default so unknown fields don't cause 500s in production.
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
  }

  @Bean
  public RestTemplate restTemplate() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(5_000);
    factory.setReadTimeout(30_000);
    return new RestTemplate(factory);
  }
}
