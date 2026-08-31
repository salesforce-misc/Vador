/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config.base

import com.salesforce.vador.config.FieldConfigBuilder
import com.salesforce.vador.config.IDConfigBuilder
import com.salesforce.vador.specs.specs.base.BaseSpec
import com.salesforce.vador.types.Spec
import com.salesforce.vador.types.Specs
import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorEtr
import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function1
import io.vavr.Function2
import io.vavr.Tuple2
import java.lang.reflect.Type
import java.util.Optional
import java.util.function.Predicate
import org.immutables.value.Value
import org.jetbrains.annotations.Nullable

@Suppress("REDUNDANT_PROJECTION")
abstract class BaseValidationConfig<ValidatableT, FailureT> {

  abstract val shouldHaveFieldsOrFailWith: Map<TypedPropertyGetter<ValidatableT, *>, FailureT>

  @get:Nullable
  abstract val shouldHaveFieldsOrFailWithFn:
    Tuple2<Collection<TypedPropertyGetter<ValidatableT, *>>, Function2<String, Any, FailureT?>>?

  abstract val shouldHaveFieldOrFailWithFn:
    Map<TypedPropertyGetter<ValidatableT, *>, Function2<String, Any, FailureT>>

  abstract val withIdConfigs: Collection<IDConfigBuilder<*, ValidatableT, FailureT, *>>

  abstract val withFieldConfigs: Collection<FieldConfigBuilder<*, ValidatableT, FailureT>>

  @get:Nullable abstract val specify: Specs<ValidatableT, FailureT>?

  abstract val withSpecs: Collection<Spec<ValidatableT, FailureT>>

  abstract val withValidatorEtrs: Collection<ValidatorEtr<ValidatableT, FailureT>>

  @get:Nullable
  abstract val withValidators:
    Tuple2<Collection<@JvmWildcard Validator<in ValidatableT, FailureT?>>, FailureT>?

  @get:Nullable abstract val forAnnotations: Tuple2<Map<String, FailureT?>, FailureT?>?

  /**
   * `withValidators` is used for the above combination. `withValidator` is meant to be used when
   * passing individual parameters like:
   *
   * ValidationConfig.<Bean, ValidationFailure>toValidate() .withValidator(validator1, failure1)
   * .withValidator(validator2, failure2)
   */
  abstract val withValidator: Map<out Validator<in ValidatableT, FailureT>, FailureT>

  @get:Nullable abstract val withRecursiveMapper: Function1<ValidatableT, List<ValidatableT>>?

  // ! TODO 05/08/21 gopala.akshintala: Migrate them to be used with custom assertions
  @get:Value.NonAttribute
  val specs: List<BaseSpec<ValidatableT, FailureT>>
    get() = getSpecsEx()

  @Value.NonAttribute
  fun getPredicateOfSpecForTest(nameForTest: String): Optional<Predicate<ValidatableT?>> =
    getPredicateOfSpecForTestEx(nameForTest)

  @Value.NonAttribute
  fun getRequiredFieldNames(beanClass: Class<ValidatableT>): Set<String> =
    getRequiredFieldNamesEx(beanClass)

  @Value.NonAttribute fun getValidatableType(): Type? = getValidatableType(this)
}
