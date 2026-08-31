/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.specs.specs.base

import java.util.function.Predicate
import org.immutables.value.Value
import org.jetbrains.annotations.Nullable

abstract class BaseSpec<ValidatableT, FailureT> {

  @get:Nullable abstract val nameForTest: String?

  @get:Nullable abstract val orFailWith: FailureT?

  abstract fun toPredicate(): Predicate<@Nullable ValidatableT?>

  // TODO 05/06/21 gopala.akshintala: Replace with `when` expression checking instanceOf
  @Suppress("unused")
  @Value.NonAttribute
  open fun getFailure(validatable: ValidatableT?): FailureT? = orFailWith

  companion object {
    const val INVALID_FAILURE_CONFIG: String =
      "For Spec with: %s Either 'orFailWith' or 'orFailWithFn' should be passed, but not both"
  }
}
