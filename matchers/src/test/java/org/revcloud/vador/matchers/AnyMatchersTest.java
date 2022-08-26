/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vador.matchers;

import static org.hamcrest.Matchers.lessThan;
import static org.revcloud.vador.matchers.IntMatchers.inRangeInclusive;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AnyMatchersTest {

  @Test
  void anyOfOrNullMatchersTest() {
    Assertions.assertTrue(AnyMatchers.anyOfOrNull(inRangeInclusive(1, 31)).matches(null));
    Assertions.assertTrue(AnyMatchers.anyOfOrNull(inRangeInclusive(1, 31)).matches(1));
    Assertions.assertFalse(AnyMatchers.anyOfOrNull(inRangeInclusive(1, 31)).matches(0));
  }

  @Test
  void anyOfMatchersTest() {
    Assertions.assertTrue(AnyMatchers.anyOf(inRangeInclusive(1, 31), lessThan(10)).matches(0));
  }
}
