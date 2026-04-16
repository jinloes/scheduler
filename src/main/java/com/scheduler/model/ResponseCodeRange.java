package com.scheduler.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** An inclusive HTTP status code range used to determine whether a job execution succeeded. */
public record ResponseCodeRange(@Min(100) @Max(599) int low, @Min(100) @Max(599) int high) {

  public ResponseCodeRange {
    if (low > high) {
      throw new IllegalArgumentException("low (" + low + ") must be <= high (" + high + ")");
    }
  }

  /** Returns {@code true} if {@code statusCode} falls within [{@code low}, {@code high}]. */
  public boolean contains(int statusCode) {
    return statusCode >= low && statusCode <= high;
  }
}
