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
import io.vavr.Tuple2
import java.util.function.Predicate
import org.hamcrest.Matcher
import org.immutables.value.Value
import org.jetbrains.annotations.Nullable

@SpecStyle
@Value.Immutable(copy = false)
internal abstract class AbstractSpec5<ValidatableT, FailureT> : BaseSpec<ValidatableT, FailureT>() {

  abstract val whenAllTheseFieldsMatch: Tuple2<Collection<Function1<ValidatableT, *>>?, Matcher<*>?>

  abstract val thenAllThoseFieldsShouldMatch:
    Tuple2<Collection<Function1<ValidatableT, *>>?, Matcher<*>?>

  @get:Nullable abstract val orFailWithFn: Function2<Collection<*>, Collection<*>, out FailureT>?

  @Suppress("UNCHECKED_CAST")
  @Value.NonAttribute
  override fun toPredicate(): Predicate<@Nullable ValidatableT?> =
    (this as Spec5<ValidatableT, FailureT>).toPredicateEx()

  abstract class Builder<ValidatableT, FailureT> : SpecBuilder<ValidatableT, FailureT>
}
