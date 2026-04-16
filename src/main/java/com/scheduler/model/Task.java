package com.scheduler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
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
 * only sent for POST, PUT, and PATCH requests.
 */
public record Task(
    @NotBlank @URL String uri,
    @NotBlank @Pattern(regexp = "GET|POST|PUT|PATCH|DELETE") String method,
    Map<String, Object> body,
    @Valid @JsonProperty("response_code_ranges")
        List<@NotNull ResponseCodeRange> responseCodeRanges) {}
