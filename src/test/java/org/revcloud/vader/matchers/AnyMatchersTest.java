package org.revcloud.vader.matchers;

import static org.revcloud.vader.matchers.AnyMatchers.anyOfOrNull;
import static org.revcloud.vader.matchers.IntMatchers.inRangeInclusive;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AnyMatchersTest {

  @Test
  void anyOfOrNullMatchersTest() {
    Assertions.assertTrue(anyOfOrNull(inRangeInclusive(1, 31)).matches(null));
    Assertions.assertTrue(anyOfOrNull(inRangeInclusive(1, 31)).matches(1));
    Assertions.assertFalse(anyOfOrNull(inRangeInclusive(1, 31)).matches(0));
  }
}
