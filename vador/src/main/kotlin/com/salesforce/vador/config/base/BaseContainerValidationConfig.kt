/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config.base

import com.salesforce.vador.config.container.getContainerValidatorsEx
import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorEtr
import io.vavr.Tuple2
import org.immutables.value.Value
import org.jetbrains.annotations.Nullable

abstract class BaseContainerValidationConfig<ContainerValidatableT, FailureT> {

  @get:Nullable abstract val shouldHaveMinBatchSizeOrFailWith: Tuple2<Int, FailureT?>?

  @get:Nullable abstract val shouldHaveMaxBatchSizeOrFailWith: Tuple2<Int, FailureT?>?

  abstract val withContainerValidatorEtrs: Collection<ValidatorEtr<ContainerValidatableT, FailureT>>

  @get:Nullable
  abstract val withContainerValidators:
    Tuple2<Collection<Validator<in ContainerValidatableT, FailureT?>>, FailureT?>?

  abstract val withContainerValidator: Map<Validator<in ContainerValidatableT, FailureT>, FailureT>

  @get:Value.NonAttribute
  val containerValidators: List<ValidatorEtr<ContainerValidatableT, FailureT?>>
    get() = getContainerValidatorsEx()
}
