/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.specs.factory;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.salesforce.vador.specs.failure.ValidationFailure;
import org.junit.jupiter.api.Test;

class SpecFactoryTest {

	@Test
	void provideBothFailWithForSpec1() {
		final var spec1 =
				new SpecFactory<SpecFactoryBean, ValidationFailure>()
						._1()
						.given(SpecFactoryBean::getValue)
						.orFailWith(ValidationFailure.INVALID_VALUE)
						.orFailWithFn(ignore -> ValidationFailure.NONE)
						.done();
		final var bean = new SpecFactoryBean("");
		assertThrows(IllegalArgumentException.class, () -> spec1.getFailure(bean));
	}

	@Test
	void provideBothFailWithForSpec2() {
		final var spec2 =
				new SpecFactory<SpecFactoryBean, ValidationFailure>()
						._2()
						.when(SpecFactoryBean::getValue)
						.then(SpecFactoryBean::getValue)
						.orFailWith(ValidationFailure.INVALID_VALUE)
						.orFailWithFn((ignore1, ignore2) -> ValidationFailure.NONE)
						.done();
		final var bean = new SpecFactoryBean("");
		assertThrows(IllegalArgumentException.class, () -> spec2.getFailure(bean));
	}

	@Test
	void provideBothFailWithForSpec3() {
		final var spec3 =
				new SpecFactory<SpecFactoryBean, ValidationFailure>()
						._3()
						.when(SpecFactoryBean::getValue)
						.thenField1(SpecFactoryBean::getValue)
						.thenField2(SpecFactoryBean::getValue)
						.orFailWith(ValidationFailure.INVALID_VALUE)
						.orFailWithFn((ignore1, ignore2, ignore3) -> ValidationFailure.NONE)
						.done();
		final var bean = new SpecFactoryBean("");
		assertThrows(IllegalArgumentException.class, () -> spec3.getFailure(bean));
	}
}
