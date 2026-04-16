package com.scheduler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;
import org.hibernate.validator.constraints.URL;

/**
 * Describes the HTTP callback to execute when a job fires.
 *
 * <p>{@code method} must be one of: GET, POST, PUT, PATCH, DELETE (case-sensitive). {@code body} is
 * only sent for POST, PUT, and PATCH requests. {@code maxRetries} and {@code retryBackoffMs}
 * control retry behaviour on failure; both default to {@code 3} retries and {@code 1000 ms} backoff
 * when omitted.
 */
public record Task(
    @NotBlank @URL String uri,
    @NotBlank @Pattern(regexp = "GET|POST|PUT|PATCH|DELETE") String method,
    Map<String, Object> body,
    @Valid @JsonProperty("response_code_ranges")
        List<@NotNull ResponseCodeRange> responseCodeRanges,
    @Min(0) @Max(10) @JsonProperty("max_retries") Integer maxRetries,
    @Min(0) @JsonProperty("retry_backoff_ms") Long retryBackoffMs) {}
