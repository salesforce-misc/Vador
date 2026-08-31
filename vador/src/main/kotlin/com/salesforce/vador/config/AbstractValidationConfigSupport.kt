/**
 * ****************************************************************************
 * Copyright (c) 2026, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config

import com.salesforce.vador.config.base.BaseBatchValidationConfig
import com.salesforce.vador.config.base.BaseValidationConfig
import com.salesforce.vador.types.Spec
import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorEtr
import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function2
import org.immutables.value.Value

internal abstract class AbstractValidationConfigSupport<ValidatableT, FailureT> :
  BaseValidationConfig<ValidatableT, FailureT>() {

  @get:Value.Auxiliary
  protected abstract val shouldHaveFieldsOrFailWiths:
    Map<TypedPropertyGetter<ValidatableT, *>, FailureT>

  @get:Value.Derived
  override val shouldHaveFieldsOrFailWith: Map<TypedPropertyGetter<ValidatableT, *>, FailureT>
    get() = shouldHaveFieldsOrFailWiths

  @get:Value.Auxiliary
  protected abstract val shouldHaveFieldOrFailWithFns:
    Map<TypedPropertyGetter<ValidatableT, *>, Function2<String, Any, FailureT>>

  @get:Value.Derived
  override val shouldHaveFieldOrFailWithFn:
    Map<TypedPropertyGetter<ValidatableT, *>, Function2<String, Any, FailureT>>
    get() = shouldHaveFieldOrFailWithFns

  @get:Value.Auxiliary
  protected abstract val idConfigBuilders: List<IDConfigBuilder<ValidatableT, FailureT>>

  @get:Value.Derived
  override val withIdConfigs: Collection<IDConfigBuilder<ValidatableT, FailureT>>
    get() = idConfigBuilders

  @get:Value.Auxiliary
  protected abstract val fieldConfigBuilders: List<FieldConfigBuilder<ValidatableT, FailureT>>

  @get:Value.Derived
  override val withFieldConfigs: Collection<FieldConfigBuilder<ValidatableT, FailureT>>
    get() = fieldConfigBuilders

  @get:Value.Auxiliary protected abstract val validationSpecs: List<Spec<ValidatableT, FailureT>>

  @get:Value.Derived
  override val withSpecs: Collection<Spec<ValidatableT, FailureT>>
    get() = validationSpecs

  @get:Value.Auxiliary
  protected abstract val validationValidatorEtrs: List<ValidatorEtr<ValidatableT?, FailureT?>>

  @get:Value.Derived
  override val withValidatorEtrs: Collection<ValidatorEtr<ValidatableT?, FailureT?>>
    get() = validationValidatorEtrs

  @get:Value.Auxiliary
  protected abstract val withValidatorMappings:
    Map<Validator<in ValidatableT?, FailureT?>, FailureT?>

  @get:Value.Derived
  override val withValidator: Map<Validator<in ValidatableT?, FailureT?>, FailureT?>
    get() = withValidatorMappings
}

internal abstract class AbstractBatchValidationConfigSupport<ValidatableT, FailureT> :
  BaseBatchValidationConfig<ValidatableT, FailureT>() {

  @get:Value.Auxiliary
  protected abstract val shouldHaveFieldsOrFailWiths:
    Map<TypedPropertyGetter<ValidatableT, *>, FailureT>

  @get:Value.Derived
  override val shouldHaveFieldsOrFailWith: Map<TypedPropertyGetter<ValidatableT, *>, FailureT>
    get() = shouldHaveFieldsOrFailWiths

  @get:Value.Auxiliary
  protected abstract val shouldHaveFieldOrFailWithFns:
    Map<TypedPropertyGetter<ValidatableT, *>, Function2<String, Any, FailureT>>

  @get:Value.Derived
  override val shouldHaveFieldOrFailWithFn:
    Map<TypedPropertyGetter<ValidatableT, *>, Function2<String, Any, FailureT>>
    get() = shouldHaveFieldOrFailWithFns

  @get:Value.Auxiliary
  protected abstract val idConfigBuilders: List<IDConfigBuilder<ValidatableT, FailureT>>

  @get:Value.Derived
  override val withIdConfigs: Collection<IDConfigBuilder<ValidatableT, FailureT>>
    get() = idConfigBuilders

  @get:Value.Auxiliary
  protected abstract val fieldConfigBuilders: List<FieldConfigBuilder<ValidatableT, FailureT>>

  @get:Value.Derived
  override val withFieldConfigs: Collection<FieldConfigBuilder<ValidatableT, FailureT>>
    get() = fieldConfigBuilders

  @get:Value.Auxiliary protected abstract val validationSpecs: List<Spec<ValidatableT, FailureT>>

  @get:Value.Derived
  override val withSpecs: Collection<Spec<ValidatableT, FailureT>>
    get() = validationSpecs

  @get:Value.Auxiliary
  protected abstract val validationValidatorEtrs: List<ValidatorEtr<ValidatableT?, FailureT?>>

  @get:Value.Derived
  override val withValidatorEtrs: Collection<ValidatorEtr<ValidatableT?, FailureT?>>
    get() = validationValidatorEtrs

  @get:Value.Auxiliary
  protected abstract val withValidatorMappings:
    Map<Validator<in ValidatableT?, FailureT?>, FailureT?>

  @get:Value.Derived
  override val withValidator: Map<Validator<in ValidatableT?, FailureT?>, FailureT?>
    get() = withValidatorMappings

  @get:Value.Auxiliary
  protected abstract val filterDuplicatesConfigBuilders:
    List<FilterDuplicatesConfigBuilder<ValidatableT, FailureT?>>

  @get:Value.Derived
  override val findAndFilterDuplicatesConfigs:
    Collection<FilterDuplicatesConfigBuilder<ValidatableT, FailureT?>>
    get() = filterDuplicatesConfigBuilders
}
