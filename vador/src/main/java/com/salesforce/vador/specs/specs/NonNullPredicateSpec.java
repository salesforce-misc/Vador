/*******************************************************************************
 * Copyright (c) 2026, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.specs.specs;

import com.salesforce.vador.specs.specs.base.BaseSpec;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

abstract class NonNullPredicateSpec<ValidatableT, FailureT>
		extends BaseSpec<ValidatableT, FailureT> {

	@Override
	public abstract Predicate<@NotNull ValidatableT> toPredicate();
}
