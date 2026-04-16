package com.scheduler.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ResponseCodeRangeTest {

  @Nested
  class Contains {

    @Test
    void returnsTrue_forBoundaryValues() {
      ResponseCodeRange range = new ResponseCodeRange(200, 299);
      assertThat(range.contains(200)).isTrue();
      assertThat(range.contains(299)).isTrue();
    }

    @Test
    void returnsTrue_forMidRangeValue() {
      assertThat(new ResponseCodeRange(200, 299).contains(250)).isTrue();
    }

    @Test
    void returnsFalse_forValuesOutsideRange() {
      ResponseCodeRange range = new ResponseCodeRange(200, 299);
      assertThat(range.contains(199)).isFalse();
      assertThat(range.contains(300)).isFalse();
    }

    @Test
    void worksForSingleCodeRange() {
      ResponseCodeRange range = new ResponseCodeRange(404, 404);
      assertThat(range.contains(404)).isTrue();
      assertThat(range.contains(403)).isFalse();
      assertThat(range.contains(405)).isFalse();
    }
  }

  @Nested
  class Constructor {

    @Test
    void throwsWhenLowGreaterThanHigh() {
      assertThatThrownBy(() -> new ResponseCodeRange(299, 200))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("low")
          .hasMessageContaining("high");
    }

    @Test
    void allowsEqualLowAndHigh() {
      ResponseCodeRange range = new ResponseCodeRange(200, 200);
      assertThat(range.contains(200)).isTrue();
    }
  }
}
