/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
@file:JvmName("ValidatorLiftUtil")

package com.salesforce.vador.lift

import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorEtr
import io.vavr.kotlin.left

/**
 * Lifts Simple Validator to Validator type.
 *
 * @param toBeLifted Simple validator to be lifted
 * @param none Value to be returned in case of no failures
 * @param <FailureT>
 * @param <ValidatableT>
 * @return Validator </ValidatableT></FailureT>
 */
fun <FailureT, ValidatableT> liftToEtr(
  toBeLifted: Validator<in ValidatableT?, FailureT?>,
  none: FailureT?,
): ValidatorEtr<ValidatableT?, FailureT?> = ValidatorEtr {
  it.flatMap { validatable ->
    val result = toBeLifted.unchecked().apply(validatable)
    if (result == none) it else left(result)
  }
}

/**
 * Lifts a list of Simple validators to list of Validator type.
 *
 * @param toBeLiftedFns List of Simple functions to be lifted.
 * @param none Value to be returned in case of no failures.
 * @param <FailureT>
 * @param <ValidatableT>
 * @return List of Validators </ValidatableT></FailureT>
 */
fun <FailureT, ValidatableT> liftAllToEtr(
  toBeLiftedFns: Collection<Validator<in ValidatableT?, FailureT?>>,
  none: FailureT,
): List<ValidatorEtr<ValidatableT?, FailureT?>> = toBeLiftedFns.map { liftToEtr(it, none) }
