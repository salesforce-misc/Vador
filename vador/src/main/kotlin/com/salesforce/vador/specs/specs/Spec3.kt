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
import io.vavr.Function3
import java.util.function.Predicate
import org.hamcrest.Matcher
import org.immutables.value.Value
import org.jetbrains.annotations.Nullable

@SpecStyle
@Value.Immutable(copy = false)
internal abstract class AbstractSpec3<ValidatableT, FailureT, WhenT, Then1T, Then2T> :
  BaseSpec<ValidatableT, FailureT>() {

  abstract val `when`: Function1<ValidatableT, out WhenT>

  abstract val matchesAnyOf: List<Matcher<out WhenT>>

  abstract val thenField1: Function1<ValidatableT, out Then1T>

  abstract val thenField2: Function1<ValidatableT, out Then2T>

  @get:Value.Auxiliary protected abstract val relations: Map<Then1T, Set<Then2T>>

  @Suppress("REDUNDANT_PROJECTION")
  @get:Value.Derived
  open val shouldRelateWith: Map<out Then1T, out Set<out Then2T>>
    get() = relations

  @get:Nullable abstract val shouldRelateWithFn: Function2<Then1T, Then2T, Boolean>?

  abstract val orField1ShouldMatchAnyOf: List<Matcher<out Then1T>>

  abstract val orField2ShouldMatchAnyOf: List<Matcher<out Then2T>>

  @get:Nullable abstract val orFailWithFn: Function3<WhenT, Then1T, Then2T, out FailureT>?

  @Suppress("UNCHECKED_CAST")
  @Value.NonAttribute
  override fun toPredicate(): Predicate<@Nullable ValidatableT?> =
    (this as Spec3<ValidatableT, FailureT, WhenT, Then1T, Then2T>).toPredicateEx()

  @Suppress("UNCHECKED_CAST")
  @Value.NonAttribute
  override fun getFailure(validatable: ValidatableT?): FailureT? =
    (this as Spec3<ValidatableT, FailureT, WhenT, Then1T, Then2T>).getFailureEx(validatable)

  abstract class Builder<ValidatableT, FailureT, WhenT, Then1T, Then2T> :
    SpecBuilder<ValidatableT, FailureT>,
    Spec3BuilderDsl<
      WhenT,
      Then1T,
      Then2T,
      Spec3.Builder<ValidatableT, FailureT, WhenT, Then1T, Then2T>,
    >
}
