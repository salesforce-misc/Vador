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
internal abstract class AbstractSpec4<ValidatableT, FailureT> : BaseSpec<ValidatableT, FailureT>() {

  abstract val whenTheseFieldsMatch: Map<Function1<ValidatableT, *>, Matcher<*>>

  abstract val thenThoseFieldsShouldMatch: Map<Function1<ValidatableT, *>, Matcher<*>>

  @get:Nullable abstract val orFailWithFn: Function2<Collection<*>, Collection<*>, out FailureT>?

  @Suppress("UNCHECKED_CAST")
  @Value.NonAttribute
  override fun toPredicate(): Predicate<ValidatableT?> =
    (this as Spec4<ValidatableT, FailureT>).toPredicateEx()

  abstract class Builder<ValidatableT, FailureT> :
    SpecBuilder<ValidatableT, FailureT>,
    Spec4BuilderDsl<ValidatableT, Spec4.Builder<ValidatableT, FailureT>>
}
