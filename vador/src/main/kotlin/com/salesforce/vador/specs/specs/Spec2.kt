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
import io.vavr.Function2
import java.util.function.Predicate
import org.hamcrest.Matcher
import org.immutables.value.Value
import org.jetbrains.annotations.Nullable

@SpecStyle
@Value.Immutable(copy = false)
internal abstract class AbstractSpec2<ValidatableT, FailureT, WhenT, ThenT> :
  BaseSpec<ValidatableT, FailureT>() {

  abstract val `when`: Function1<ValidatableT, out WhenT>

  abstract val matchesAnyOf: List<Matcher<out WhenT>>

  abstract val then: Function1<ValidatableT, out ThenT>

  // TODO 28/04/21 gopala.akshintala: Think about having `or` prefix
  abstract val shouldMatchAnyOf: List<Matcher<out ThenT>>

  @get:Value.Auxiliary protected abstract val relations: Map<WhenT, Set<ThenT>>

  @Suppress("REDUNDANT_PROJECTION")
  @get:Value.Derived
  open val shouldRelateWith: Map<out WhenT, out Set<out ThenT>>
    get() = relations

  @get:Nullable abstract val shouldRelateWithFn: Function2<WhenT, ThenT, Boolean>?

  @get:Nullable abstract val orFailWithFn: Function2<WhenT, ThenT, out FailureT>?

  @Suppress("UNCHECKED_CAST")
  @Value.NonAttribute
  override fun toPredicate(): Predicate<@Nullable ValidatableT?> =
    (this as Spec2<ValidatableT, FailureT, WhenT, ThenT>).toPredicateEx()

  @Value.NonAttribute
  override fun getFailure(validatable: ValidatableT?): FailureT? {
    if ((orFailWith == null) == (orFailWithFn == null)) {
      throw IllegalArgumentException(String.format(INVALID_FAILURE_CONFIG, nameForTest))
    }
    if (orFailWith != null) {
      return orFailWith
    }
    return orFailWithFn?.apply(`when`.apply(validatable), then.apply(validatable))
  }

  abstract class Builder<ValidatableT, FailureT, WhenT, ThenT> :
    SpecBuilder<ValidatableT, FailureT>,
    Spec2BuilderDsl<WhenT, ThenT, Spec2.Builder<ValidatableT, FailureT, WhenT, ThenT>>
}
