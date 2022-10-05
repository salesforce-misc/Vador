/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.execution.strategies

import com.salesforce.vador.execution.strategies.util.fireValidators
import com.salesforce.vador.types.ValidatorEtr
import io.vavr.control.Either
import io.vavr.kotlin.right

/**
 * Higher-order function to compose list of validators into Accumulation Strategy.
 *
 * @param validators
 * @param invalidValidatable
 * @param <FailureT>
 * @param <ValidatableT>
 * @return Composed Accumulation Strategy
 */
@JvmSynthetic
internal fun <FailureT, ValidatableT> accumulationStrategy(
  validators: List<ValidatorEtr<ValidatableT?, FailureT?>>,
  throwableMapper: (Throwable) -> FailureT?
): Accumulation<ValidatableT, FailureT> = {
  fireValidators(right(it), validators, throwableMapper).toList()
}

internal typealias Accumulation<ValidatableT, FailureT> = (ValidatableT) -> List<Either<FailureT?, ValidatableT?>>
