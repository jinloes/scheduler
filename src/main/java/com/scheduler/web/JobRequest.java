package com.scheduler.web;

import com.scheduler.model.Task;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Request body for {@code POST /api/v1/jobs}. */
public record JobRequest(@NotNull @Valid Task task, @NotBlank String schedule) {}
