/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config

import com.salesforce.vador.immutables.ConfigStyle
import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function2
import io.vavr.Tuple2
import java.util.function.Predicate
import org.immutables.value.Value
import org.jetbrains.annotations.Nullable

@ConfigStyle
@Value.Immutable(copy = false)
internal abstract class AbstractFieldConfig<FieldT, ValidatableT, FailureT> {
  @get:Nullable abstract val withFieldValidator: Predicate<FieldT>?

  @get:Value.Auxiliary
  protected abstract val shouldHaveValidFormatForAllOrFailWiths:
    Map<TypedPropertyGetter<ValidatableT, FieldT>, FailureT>

  @get:Value.Derived
  open val shouldHaveValidFormatForAllOrFailWith:
    Map<TypedPropertyGetter<ValidatableT, FieldT>, FailureT>
    get() = shouldHaveValidFormatForAllOrFailWiths

  @get:Nullable
  abstract val shouldHaveValidFormatForAllOrFailWithFn:
    Tuple2<
      Collection<TypedPropertyGetter<ValidatableT, FieldT?>>,
      Function2<String, FieldT?, FailureT?>,
    >?

  @get:Value.Auxiliary
  protected abstract val shouldHaveValidFormatOrFailWithFns:
    Map<TypedPropertyGetter<ValidatableT, FieldT>, Function2<String, FieldT, FailureT>>

  @get:Value.Derived
  open val shouldHaveValidFormatOrFailWithFn:
    Map<TypedPropertyGetter<ValidatableT, FieldT>, Function2<String, FieldT, FailureT>>
    get() = shouldHaveValidFormatOrFailWithFns

  @get:Value.Auxiliary
  protected abstract val absentOrHaveValidFormatForAllOrFailWiths:
    Map<TypedPropertyGetter<ValidatableT, FieldT>, FailureT>

  @get:Value.Derived
  open val absentOrHaveValidFormatForAllOrFailWith:
    Map<TypedPropertyGetter<ValidatableT, FieldT>, FailureT>
    get() = absentOrHaveValidFormatForAllOrFailWiths

  @get:Nullable
  abstract val absentOrHaveValidFormatForAllOrFailWithFn:
    Tuple2<
      Collection<TypedPropertyGetter<ValidatableT, FieldT?>>,
      Function2<String, FieldT?, FailureT?>,
    >?

  @get:Value.Auxiliary
  protected abstract val absentOrHaveValidFormatOrFailWithFns:
    Map<TypedPropertyGetter<ValidatableT, FieldT>, Function2<String, FieldT, FailureT>>

  @get:Value.Derived
  open val absentOrHaveValidFormatOrFailWithFn:
    Map<TypedPropertyGetter<ValidatableT, FieldT>, Function2<String, FieldT, FailureT>>
    get() = absentOrHaveValidFormatOrFailWithFns

  abstract class Builder<FieldT, ValidatableT, FailureT> :
    FieldConfigBuilderDsl<
      FieldT,
      ValidatableT,
      FailureT,
      FieldConfig.Builder<FieldT, ValidatableT, FailureT>,
    >
}
