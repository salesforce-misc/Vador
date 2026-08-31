/**
 * ****************************************************************************
 * Copyright (c) 2026, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config

import com.salesforce.vador.types.Spec
import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorEtr
import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function2
import io.vavr.Tuple2

internal interface ValidationBuilderDsl<ValidatableT, FailureT, SELF> {
  fun shouldHaveFieldsOrFailWith(
    key: TypedPropertyGetter<ValidatableT, *>?,
    failure: FailureT?,
  ): SELF

  fun shouldHaveFieldOrFailWith(
    key: TypedPropertyGetter<ValidatableT, *>?,
    failure: FailureT?,
  ): SELF = shouldHaveFieldsOrFailWith(key, failure)

  fun putAllShouldHaveFieldsOrFailWiths(
    entries: Map<out TypedPropertyGetter<ValidatableT, *>, FailureT>
  ): SELF

  fun shouldHaveFieldsOrFailWith(
    entries: Map<out TypedPropertyGetter<ValidatableT, *>, FailureT>
  ): SELF = putAllShouldHaveFieldsOrFailWiths(entries)

  fun putAllShouldHaveFieldOrFailWithFns(
    entries: Map<out TypedPropertyGetter<ValidatableT, *>, Function2<String, Any, FailureT>>
  ): SELF

  fun shouldHaveFieldOrFailWithFn(
    entries: Map<out TypedPropertyGetter<ValidatableT, *>, Function2<String, Any, FailureT>>
  ): SELF = putAllShouldHaveFieldOrFailWithFns(entries)

  fun idConfigBuilder(value: IDConfigBuilder<*, ValidatableT, FailureT, *>): SELF

  fun addAllIdConfigBuilders(values: Iterable<IDConfigBuilder<*, ValidatableT, FailureT, *>>): SELF

  fun withIdConfig(value: IDConfigBuilder<*, ValidatableT, FailureT, *>): SELF =
    idConfigBuilder(value)

  fun withIdConfigs(values: Iterable<IDConfigBuilder<*, ValidatableT, FailureT, *>>): SELF =
    addAllIdConfigBuilders(values)

  fun fieldConfigBuilder(value: FieldConfigBuilder<*, ValidatableT, FailureT>): SELF

  fun addAllFieldConfigBuilders(
    values: Iterable<FieldConfigBuilder<*, ValidatableT, FailureT>>
  ): SELF

  fun withFieldConfig(value: FieldConfigBuilder<*, ValidatableT, FailureT>): SELF =
    fieldConfigBuilder(value)

  fun withFieldConfigs(values: Iterable<FieldConfigBuilder<*, ValidatableT, FailureT>>): SELF =
    addAllFieldConfigBuilders(values)

  fun validationSpec(value: Spec<ValidatableT, FailureT>): SELF

  fun addAllValidationSpecs(values: Iterable<Spec<ValidatableT, FailureT>>): SELF

  fun withSpec(value: Spec<ValidatableT, FailureT>): SELF = validationSpec(value)

  fun withSpecs(values: Iterable<Spec<ValidatableT, FailureT>>): SELF =
    addAllValidationSpecs(values)

  fun validationValidatorEtr(value: ValidatorEtr<ValidatableT, FailureT>): SELF

  fun addAllValidationValidatorEtrs(values: Iterable<ValidatorEtr<ValidatableT, FailureT>>): SELF

  fun withValidatorEtr(value: ValidatorEtr<ValidatableT, FailureT>): SELF =
    validationValidatorEtr(value)

  fun withValidatorEtrs(values: Iterable<ValidatorEtr<ValidatableT, FailureT>>): SELF =
    addAllValidationValidatorEtrs(values)

  fun withValidatorMapping(key: Validator<in ValidatableT, FailureT>, value: FailureT): SELF

  fun putAllWithValidatorMappings(
    entries: Map<out Validator<in ValidatableT, FailureT>, FailureT>
  ): SELF

  fun withValidator(key: Validator<in ValidatableT, FailureT>, value: FailureT): SELF =
    withValidatorMapping(key, value)

  fun withValidator(entries: Map<out Validator<in ValidatableT, FailureT>, FailureT>): SELF =
    putAllWithValidatorMappings(entries)

  fun withValidators(
    value: Tuple2<Collection<@JvmWildcard Validator<in ValidatableT, FailureT?>>, FailureT>?
  ): SELF

  fun forAnnotations(value: Tuple2<Map<String, FailureT?>, FailureT?>?): SELF
}

internal interface BatchValidationBuilderDsl<ValidatableT, FailureT, SELF> {
  fun filterDuplicatesConfigBuilder(
    value: FilterDuplicatesConfigBuilder<ValidatableT, FailureT?>
  ): SELF

  fun addAllFilterDuplicatesConfigBuilders(
    values: Iterable<FilterDuplicatesConfigBuilder<ValidatableT, FailureT?>>
  ): SELF

  fun findAndFilterDuplicatesConfig(
    value: FilterDuplicatesConfigBuilder<ValidatableT, FailureT?>
  ): SELF = filterDuplicatesConfigBuilder(value)

  fun findAndFilterDuplicatesConfigs(
    values: Iterable<FilterDuplicatesConfigBuilder<ValidatableT, FailureT?>>
  ): SELF = addAllFilterDuplicatesConfigBuilders(values)
}

@Suppress("REDUNDANT_PROJECTION")
internal interface FieldConfigBuilderDsl<FieldT, ValidatableT, FailureT, SELF> {
  fun shouldHaveValidFormatForAllOrFailWith(
    key: TypedPropertyGetter<ValidatableT, FieldT>,
    value: FailureT,
  ): SELF

  fun shouldHaveValidFormatOrFailWith(
    key: TypedPropertyGetter<ValidatableT, FieldT>,
    value: FailureT,
  ): SELF = shouldHaveValidFormatForAllOrFailWith(key, value)

  fun putAllShouldHaveValidFormatForAllOrFailWiths(
    entries: Map<out TypedPropertyGetter<ValidatableT, FieldT>, FailureT>
  ): SELF

  fun shouldHaveValidFormatForAllOrFailWith(
    entries: Map<out TypedPropertyGetter<ValidatableT, FieldT>, FailureT>
  ): SELF = putAllShouldHaveValidFormatForAllOrFailWiths(entries)

  fun putAllShouldHaveValidFormatOrFailWithFns(
    entries: Map<out TypedPropertyGetter<ValidatableT, FieldT>, Function2<String, FieldT, FailureT>>
  ): SELF

  fun shouldHaveValidFormatOrFailWithFn(
    entries: Map<out TypedPropertyGetter<ValidatableT, FieldT>, Function2<String, FieldT, FailureT>>
  ): SELF = putAllShouldHaveValidFormatOrFailWithFns(entries)

  fun absentOrHaveValidFormatForAllOrFailWith(
    key: TypedPropertyGetter<ValidatableT, FieldT>,
    value: FailureT,
  ): SELF

  fun absentOrHaveValidFormatOrFailWith(
    key: TypedPropertyGetter<ValidatableT, FieldT>,
    value: FailureT,
  ): SELF = absentOrHaveValidFormatForAllOrFailWith(key, value)

  fun putAllAbsentOrHaveValidFormatForAllOrFailWiths(
    entries: Map<out TypedPropertyGetter<ValidatableT, FieldT>, FailureT>
  ): SELF

  fun absentOrHaveValidFormatForAllOrFailWith(
    entries: Map<out TypedPropertyGetter<ValidatableT, FieldT>, FailureT>
  ): SELF = putAllAbsentOrHaveValidFormatForAllOrFailWiths(entries)

  fun putAllAbsentOrHaveValidFormatOrFailWithFns(
    entries: Map<out TypedPropertyGetter<ValidatableT, FieldT>, Function2<String, FieldT, FailureT>>
  ): SELF

  fun absentOrHaveValidFormatOrFailWithFn(
    entries: Map<out TypedPropertyGetter<ValidatableT, FieldT>, Function2<String, FieldT, FailureT>>
  ): SELF = putAllAbsentOrHaveValidFormatOrFailWithFns(entries)
}

@Suppress("REDUNDANT_PROJECTION")
internal interface IDConfigBuilderDsl<IDT, ValidatableT, FailureT, EntityIdInfoT, SELF> {
  fun shouldHaveValidSFIdFormatForAllOrFailWith(
    key: Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
    value: FailureT?,
  ): SELF

  fun shouldHaveValidSFIdFormatOrFailWith(
    key: Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
    value: FailureT?,
  ): SELF = shouldHaveValidSFIdFormatForAllOrFailWith(key, value)

  fun putAllShouldHaveValidSFIdFormatForAllOrFailWiths(
    entries: Map<out Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>, FailureT?>
  ): SELF

  fun shouldHaveValidSFIdFormatForAllOrFailWith(
    entries: Map<out Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>, FailureT?>
  ): SELF = putAllShouldHaveValidSFIdFormatForAllOrFailWiths(entries)

  fun shouldHaveValidSFPolymorphicIdFormatForAllOrFailWith(
    key:
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
    value: FailureT?,
  ): SELF

  fun shouldHaveValidSFPolymorphicIdFormatOrFailWith(
    key:
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
    value: FailureT?,
  ): SELF = shouldHaveValidSFPolymorphicIdFormatForAllOrFailWith(key, value)

  fun putAllShouldHaveValidSFPolymorphicIdFormatForAllOrFailWiths(
    entries:
      Map<
        out Tuple2<
          TypedPropertyGetter<ValidatableT, IDT?>,
          out Collection<@JvmWildcard EntityIdInfoT>,
        >,
        FailureT?,
      >
  ): SELF

  fun shouldHaveValidSFPolymorphicIdFormatForAllOrFailWith(
    entries:
      Map<
        out Tuple2<
          TypedPropertyGetter<ValidatableT, IDT?>,
          out Collection<@JvmWildcard EntityIdInfoT>,
        >,
        FailureT?,
      >
  ): SELF = putAllShouldHaveValidSFPolymorphicIdFormatForAllOrFailWiths(entries)

  fun putAllShouldHaveValidSFIdFormatOrFailWithFns(
    entries:
      Map<
        out Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
        Function2<String, IDT?, FailureT?>,
      >
  ): SELF

  fun shouldHaveValidSFIdFormatOrFailWithFn(
    entries:
      Map<
        out Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
        Function2<String, IDT?, FailureT?>,
      >
  ): SELF = putAllShouldHaveValidSFIdFormatOrFailWithFns(entries)

  fun putAllShouldHaveValidSFPolymorphicIdFormatOrFailWithFns(
    entries:
      Map<
        out Tuple2<
          TypedPropertyGetter<ValidatableT, IDT?>,
          out Collection<@JvmWildcard EntityIdInfoT>,
        >,
        Function2<String, IDT?, FailureT?>,
      >
  ): SELF

  fun shouldHaveValidSFPolymorphicIdFormatOrFailWithFn(
    entries:
      Map<
        out Tuple2<
          TypedPropertyGetter<ValidatableT, IDT?>,
          out Collection<@JvmWildcard EntityIdInfoT>,
        >,
        Function2<String, IDT?, FailureT?>,
      >
  ): SELF = putAllShouldHaveValidSFPolymorphicIdFormatOrFailWithFns(entries)

  fun absentOrHaveValidSFIdFormatForAllOrFailWith(
    key: Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
    value: FailureT?,
  ): SELF

  fun absentOrHaveValidSFIdFormatOrFailWith(
    key: Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
    value: FailureT?,
  ): SELF = absentOrHaveValidSFIdFormatForAllOrFailWith(key, value)

  fun putAllAbsentOrHaveValidSFIdFormatForAllOrFailWiths(
    entries: Map<out Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>, FailureT?>
  ): SELF

  fun absentOrHaveValidSFIdFormatForAllOrFailWith(
    entries: Map<out Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>, FailureT?>
  ): SELF = putAllAbsentOrHaveValidSFIdFormatForAllOrFailWiths(entries)

  fun absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWith(
    key:
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
    value: FailureT?,
  ): SELF

  fun absentOrHaveValidSFPolymorphicIdFormatOrFailWith(
    key:
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<@JvmWildcard EntityIdInfoT>>,
    value: FailureT?,
  ): SELF = absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWith(key, value)

  fun putAllAbsentOrHaveValidSFPolymorphicIdFormatForAllOrFailWiths(
    entries:
      Map<
        out Tuple2<
          TypedPropertyGetter<ValidatableT, IDT?>,
          out Collection<@JvmWildcard EntityIdInfoT>,
        >,
        FailureT?,
      >
  ): SELF

  fun absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWith(
    entries:
      Map<
        out Tuple2<
          TypedPropertyGetter<ValidatableT, IDT?>,
          out Collection<@JvmWildcard EntityIdInfoT>,
        >,
        FailureT?,
      >
  ): SELF = putAllAbsentOrHaveValidSFPolymorphicIdFormatForAllOrFailWiths(entries)

  fun putAllAbsentOrHaveValidSFIdFormatOrFailWithFns(
    entries:
      Map<
        out Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
        Function2<String, IDT?, FailureT?>,
      >
  ): SELF

  fun absentOrHaveValidSFIdFormatOrFailWithFn(
    entries:
      Map<
        out Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityIdInfoT>,
        Function2<String, IDT?, FailureT?>,
      >
  ): SELF = putAllAbsentOrHaveValidSFIdFormatOrFailWithFns(entries)

  fun putAllAbsentOrHaveValidSFPolymorphicIdFormatOrFailWithFns(
    entries:
      Map<
        out Tuple2<
          TypedPropertyGetter<ValidatableT, IDT?>,
          out Collection<@JvmWildcard EntityIdInfoT>,
        >,
        Function2<String, IDT?, FailureT?>,
      >
  ): SELF

  fun absentOrHaveValidSFPolymorphicIdFormatOrFailWithFn(
    entries:
      Map<
        out Tuple2<
          TypedPropertyGetter<ValidatableT, IDT?>,
          out Collection<@JvmWildcard EntityIdInfoT>,
        >,
        Function2<String, IDT?, FailureT?>,
      >
  ): SELF = putAllAbsentOrHaveValidSFPolymorphicIdFormatOrFailWithFns(entries)
}
