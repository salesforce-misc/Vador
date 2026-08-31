/**
 * ****************************************************************************
 * Copyright (c) 2026, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config.base

import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function2
import io.vavr.Tuple2
import java.util.function.Predicate
import org.jetbrains.annotations.Nullable

abstract class BaseFieldConfig<FieldT, ValidatableT, FailureT> {
  @get:Nullable abstract val withFieldValidator: Predicate<FieldT>?

  abstract val shouldHaveValidFormatForAllOrFailWith:
    Map<TypedPropertyGetter<ValidatableT, FieldT>, FailureT>

  @get:Nullable
  abstract val shouldHaveValidFormatForAllOrFailWithFn:
    Tuple2<
      Collection<TypedPropertyGetter<ValidatableT, FieldT?>>,
      Function2<String, FieldT?, FailureT?>,
    >?

  abstract val shouldHaveValidFormatOrFailWithFn:
    Map<TypedPropertyGetter<ValidatableT, FieldT>, Function2<String, FieldT, FailureT>>

  abstract val absentOrHaveValidFormatForAllOrFailWith:
    Map<TypedPropertyGetter<ValidatableT, FieldT>, FailureT>

  @get:Nullable
  abstract val absentOrHaveValidFormatForAllOrFailWithFn:
    Tuple2<
      Collection<TypedPropertyGetter<ValidatableT, FieldT?>>,
      Function2<String, FieldT?, FailureT?>,
    >?

  abstract val absentOrHaveValidFormatOrFailWithFn:
    Map<TypedPropertyGetter<ValidatableT, FieldT>, Function2<String, FieldT, FailureT>>
}
