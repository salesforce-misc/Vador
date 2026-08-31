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
import org.jetbrains.annotations.Nullable

abstract class BaseIDConfig<IDT, ValidatableT, FailureT, EntityIdInfoT> {
  @get:Nullable abstract val withIdValidator: Function2<IDT, EntityIdInfoT, Boolean>?

  abstract val shouldHaveValidSFIdFormatForAllOrFailWith:
    Map<Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>, FailureT?>

  abstract val shouldHaveValidSFPolymorphicIdFormatForAllOrFailWith:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
      FailureT?,
    >

  @get:Nullable
  abstract val shouldHaveValidSFIdFormatForAllOrFailWithFn:
    Tuple2<
      Map<TypedPropertyGetter<ValidatableT, IDT?>, @JvmWildcard EntityIdInfoT>,
      Function2<String, IDT?, FailureT?>,
    >?

  @get:Nullable
  abstract val shouldHaveValidSFPolymorphicIdFormatForAllOrFailWithFn:
    Tuple2<
      Map<
        TypedPropertyGetter<ValidatableT, IDT?>,
        @JvmWildcard
        Collection<@JvmWildcard EntityIdInfoT>,
      >,
      Function2<String, IDT?, FailureT?>,
    >?

  abstract val shouldHaveValidSFIdFormatOrFailWithFn:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
      Function2<String, IDT?, FailureT?>,
    >

  abstract val shouldHaveValidSFPolymorphicIdFormatOrFailWithFn:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
      Function2<String, IDT?, FailureT?>,
    >

  abstract val absentOrHaveValidSFIdFormatForAllOrFailWith:
    Map<Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>, FailureT?>

  abstract val absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWith:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
      FailureT?,
    >

  @get:Nullable
  abstract val absentOrHaveValidSFIdFormatForAllOrFailWithFn:
    Tuple2<
      Map<TypedPropertyGetter<ValidatableT, IDT>, @JvmWildcard EntityIdInfoT>,
      Function2<String, IDT?, FailureT?>,
    >?

  @get:Nullable
  abstract val absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWithFn:
    Tuple2<
      Map<
        TypedPropertyGetter<ValidatableT, IDT?>,
        @JvmWildcard
        Collection<@JvmWildcard EntityIdInfoT>,
      >,
      Function2<String, IDT?, FailureT?>,
    >?

  abstract val absentOrHaveValidSFIdFormatOrFailWithFn:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
      Function2<String, IDT?, FailureT?>,
    >

  abstract val absentOrHaveValidSFPolymorphicIdFormatOrFailWithFn:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
      Function2<String, IDT?, FailureT?>,
    >
}
