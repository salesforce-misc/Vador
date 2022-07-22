/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.specs.factory;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.revcloud.vader.specs.failure.ValidationFailure.INVALID_VALUE;
import static org.revcloud.vader.specs.failure.ValidationFailure.NONE;

import lombok.Value;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.specs.failure.ValidationFailure;

class SpecFactoryTest {

  @Test
  void provideBothFailWithForSpec1() {
    final var spec1 =
        new SpecFactory<Bean, ValidationFailure>()
            ._1()
            .given(Bean::getValue)
            .orFailWith(INVALID_VALUE)
            .orFailWithFn(ignore -> NONE)
            .done();
    final var bean = new Bean("");
    assertThrows(IllegalArgumentException.class, () -> spec1.getFailure(bean));
  }

  @Test
  void provideBothFailWithForSpec2() {
    final var spec2 =
        new SpecFactory<Bean, ValidationFailure>()
            ._2()
            .when(Bean::getValue)
            .then(Bean::getValue)
            .orFailWith(INVALID_VALUE)
            .orFailWithFn((ignore1, ignore2) -> NONE)
            .done();
    final var bean = new Bean("");
    assertThrows(IllegalArgumentException.class, () -> spec2.getFailure(bean));
  }

  @Test
  void provideBothFailWithForSpec3() {
    final var spec3 =
        new SpecFactory<Bean, ValidationFailure>()
            ._3()
            .when(Bean::getValue)
            .thenField1(Bean::getValue)
            .thenField2(Bean::getValue)
            .orFailWith(INVALID_VALUE)
            .orFailWithFn((ignore1, ignore2, ignore3) -> NONE)
            .done();
    final var bean = new Bean("");
    assertThrows(IllegalArgumentException.class, () -> spec3.getFailure(bean));
  }

  @Value
  private static class Bean {
    String value;
  }
}
