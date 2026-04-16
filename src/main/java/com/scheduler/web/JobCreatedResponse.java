package com.scheduler.web;

/** Response body for a successfully scheduled job. */
public record JobCreatedResponse(String id, String link) {}
