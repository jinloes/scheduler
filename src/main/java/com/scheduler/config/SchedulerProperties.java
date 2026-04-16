package com.scheduler.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "scheduler")
@Validated
public record SchedulerProperties(
    @Min(100) long pollRateMs,
    @Min(1) int executionThreads,
    @Min(1) @Max(10_000) int pollBatchSize) {}
