/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

@file:JvmName("InheritanceLiftEtrUtil")

package com.salesforce.vador.lift

import com.salesforce.vador.types.ValidatorEtr
import io.vavr.control.Either.narrow

/**
 * Assume `ParentT` is parent by inheritance to `ValidatableT`.
 * To validate `ValidatableT`, all it's validations + all its Parent validations has to pass.
 * These utils help to lift it's parent's validations in the context of ValidatableT, so that they can be chained together.
 */
fun <ParentT, ValidatableT : ParentT, FailureT> liftToChildValidatorType(
  parentValidatorEtr: ValidatorEtr<ParentT, FailureT>
): ValidatorEtr<ValidatableT, FailureT> =
  ValidatorEtr { childValidatable -> parentValidatorEtr.apply(narrow(childValidatable)) }

fun <ParentT, ValidatableT : ParentT, FailureT> liftAllToChildValidatorType(
  parentValidatorEtrs: Collection<ValidatorEtr<ParentT, FailureT>>
): List<ValidatorEtr<ValidatableT, FailureT>> = parentValidatorEtrs.map { liftToChildValidatorType(it) }
