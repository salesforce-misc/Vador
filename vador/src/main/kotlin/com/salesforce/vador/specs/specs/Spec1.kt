/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.specs.specs

import com.salesforce.vador.immutables.SpecStyle
import com.salesforce.vador.specs.specs.base.BaseSpec
import com.salesforce.vador.specs.specs.base.SpecBuilder
import io.vavr.Function1
import java.util.function.Predicate
import org.hamcrest.Matcher
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@SpecStyle
@Value.Style.Depluralize(dictionary = ["fields"])
@Value.Immutable(copy = false)
internal abstract class AbstractSpec1<ValidatableT, FailureT, GivenT> :
  BaseSpec<ValidatableT, FailureT>() {

  abstract val given: Function1<ValidatableT, out GivenT>

  abstract val shouldMatchAnyOfFields: List<Function1<ValidatableT, *>>

  abstract val shouldMatchAnyOf: List<Matcher<out GivenT>>

  @get:Nullable abstract val orFailWithFn: Function1<GivenT, out FailureT>?

  @Suppress("UNCHECKED_CAST")
  @Value.NonAttribute
  override fun toPredicate(): Predicate<@NotNull ValidatableT?> =
    (this as Spec1<ValidatableT, FailureT, GivenT>).toPredicateEx()

  @Value.NonAttribute
  override fun getFailure(validatable: ValidatableT?): FailureT? {
    if ((orFailWith == null) == (orFailWithFn == null)) {
      throw IllegalArgumentException(String.format(INVALID_FAILURE_CONFIG, nameForTest))
    }
    if (orFailWith != null) {
      return orFailWith
    }
    return orFailWithFn?.apply(given.apply(validatable))
  }

  abstract class Builder<ValidatableT, FailureT, GivenT> :
    SpecBuilder<ValidatableT, FailureT>,
    Spec1BuilderDsl<ValidatableT, GivenT, Spec1.Builder<ValidatableT, FailureT, GivenT>>
}
