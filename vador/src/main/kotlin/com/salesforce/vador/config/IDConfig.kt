/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config

import com.salesforce.vador.config.base.BaseIDConfig
import com.salesforce.vador.immutables.AllowNulls
import com.salesforce.vador.immutables.ConfigStyle
import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function2
import io.vavr.Tuple2
import org.immutables.value.Value
import org.jetbrains.annotations.Nullable

@ConfigStyle
@Value.Immutable(copy = false)
internal abstract class AbstractIDConfig<IDT, ValidatableT, FailureT, EntityIdInfoT> :
  BaseIDConfig<IDT, ValidatableT, FailureT, EntityIdInfoT>() {
  @get:Nullable abstract override val withIdValidator: Function2<IDT, EntityIdInfoT, Boolean>?

  @get:AllowNulls
  @get:Value.Auxiliary
  protected abstract val shouldHaveValidSFIdFormatForAllOrFailWiths:
    Map<Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>, FailureT?>

  @get:Value.Derived
  override val shouldHaveValidSFIdFormatForAllOrFailWith:
    Map<Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>, FailureT?>
    get() = shouldHaveValidSFIdFormatForAllOrFailWiths

  @get:AllowNulls
  @get:Value.Auxiliary
  protected abstract val shouldHaveValidSFPolymorphicIdFormatForAllOrFailWiths:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
      FailureT?,
    >

  @get:Value.Derived
  override val shouldHaveValidSFPolymorphicIdFormatForAllOrFailWith:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
      FailureT?,
    >
    get() = shouldHaveValidSFPolymorphicIdFormatForAllOrFailWiths

  @get:Nullable
  abstract override val shouldHaveValidSFIdFormatForAllOrFailWithFn:
    Tuple2<
      Map<TypedPropertyGetter<ValidatableT, IDT?>, @JvmWildcard EntityIdInfoT>,
      Function2<String, IDT?, FailureT?>,
    >?

  @get:Nullable
  abstract override val shouldHaveValidSFPolymorphicIdFormatForAllOrFailWithFn:
    Tuple2<
      Map<
        TypedPropertyGetter<ValidatableT, IDT?>,
        @JvmWildcard
        Collection<@JvmWildcard EntityIdInfoT>,
      >,
      Function2<String, IDT?, FailureT?>,
    >?

  @get:Value.Auxiliary
  protected abstract val shouldHaveValidSFIdFormatOrFailWithFns:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
      Function2<String, IDT?, FailureT?>,
    >

  @get:Value.Derived
  override val shouldHaveValidSFIdFormatOrFailWithFn:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
      Function2<String, IDT?, FailureT?>,
    >
    get() = shouldHaveValidSFIdFormatOrFailWithFns

  @get:Value.Auxiliary
  protected abstract val shouldHaveValidSFPolymorphicIdFormatOrFailWithFns:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
      Function2<String, IDT?, FailureT?>,
    >

  @get:Value.Derived
  override val shouldHaveValidSFPolymorphicIdFormatOrFailWithFn:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
      Function2<String, IDT?, FailureT?>,
    >
    get() = shouldHaveValidSFPolymorphicIdFormatOrFailWithFns

  @get:AllowNulls
  @get:Value.Auxiliary
  protected abstract val absentOrHaveValidSFIdFormatForAllOrFailWiths:
    Map<Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>, FailureT?>

  @get:Value.Derived
  override val absentOrHaveValidSFIdFormatForAllOrFailWith:
    Map<Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>, FailureT?>
    get() = absentOrHaveValidSFIdFormatForAllOrFailWiths

  @get:AllowNulls
  @get:Value.Auxiliary
  protected abstract val absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWiths:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
      FailureT?,
    >

  @get:Value.Derived
  override val absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWith:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
      FailureT?,
    >
    get() = absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWiths

  @get:Nullable
  abstract override val absentOrHaveValidSFIdFormatForAllOrFailWithFn:
    Tuple2<
      Map<TypedPropertyGetter<ValidatableT, IDT>, @JvmWildcard EntityIdInfoT>,
      Function2<String, IDT?, FailureT?>,
    >?

  @get:Nullable
  abstract override val absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWithFn:
    Tuple2<
      Map<
        TypedPropertyGetter<ValidatableT, IDT?>,
        @JvmWildcard
        Collection<@JvmWildcard EntityIdInfoT>,
      >,
      Function2<String, IDT?, FailureT?>,
    >?

  @get:Value.Auxiliary
  protected abstract val absentOrHaveValidSFIdFormatOrFailWithFns:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
      Function2<String, IDT?, FailureT?>,
    >

  @get:Value.Derived
  override val absentOrHaveValidSFIdFormatOrFailWithFn:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
      Function2<String, IDT?, FailureT?>,
    >
    get() = absentOrHaveValidSFIdFormatOrFailWithFns

  @get:Value.Auxiliary
  protected abstract val absentOrHaveValidSFPolymorphicIdFormatOrFailWithFns:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
      Function2<String, IDT?, FailureT?>,
    >

  @get:Value.Derived
  override val absentOrHaveValidSFPolymorphicIdFormatOrFailWithFn:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
      Function2<String, IDT?, FailureT?>,
    >
    get() = absentOrHaveValidSFPolymorphicIdFormatOrFailWithFns

  abstract class Builder<IDT, ValidatableT, FailureT, EntityIdInfoT> :
    IDConfigBuilder<IDT, ValidatableT, FailureT, EntityIdInfoT>,
    IDConfigBuilderDsl<
      IDT,
      ValidatableT,
      FailureT,
      EntityIdInfoT,
      IDConfig.Builder<IDT, ValidatableT, FailureT, EntityIdInfoT>,
    >
}
