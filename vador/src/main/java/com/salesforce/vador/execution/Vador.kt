/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
@file:JvmName("Vador")

package com.salesforce.vador.execution

import com.salesforce.vador.config.ValidationConfig
import com.salesforce.vador.config.container.ContainerValidationConfig
import com.salesforce.vador.config.container.ContainerValidationConfigWith2Levels
import com.salesforce.vador.execution.strategies.accumulationStrategy
import com.salesforce.vador.execution.strategies.failFast
import com.salesforce.vador.execution.strategies.failFastForContainer
import com.salesforce.vador.lift.liftAllToEtr
import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorEtr
import java.util.Optional

object Vador {
  /** <--- CONTAINER --- */
  @JvmStatic
  @JvmOverloads
  fun <FailureT : Any, ContainerValidatableT> validateAndFailFastForContainer(
    container: ContainerValidatableT,
    containerValidationConfig: ContainerValidationConfig<ContainerValidatableT, FailureT?>,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): Optional<FailureT> =
    failFastForContainer(containerValidationConfig, throwableMapper)(container)

  @JvmStatic
  @JvmOverloads
  fun <
    FailureT : Any,
    ContainerValidatableT,
    NestedContainerValidatableT,
  > validateAndFailFastForContainer(
    container: ContainerValidatableT,
    containerValidationConfigWith2Levels:
      ContainerValidationConfigWith2Levels<
        ContainerValidatableT,
        NestedContainerValidatableT,
        FailureT?,
      >,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): Optional<FailureT> =
    failFastForContainer(containerValidationConfigWith2Levels, throwableMapper)(container)

  /** --- CONTAINER ---> */
  @JvmStatic
  @JvmOverloads
  fun <FailureT : Any, ValidatableT> validateAndFailFast(
    validatable: ValidatableT,
    validationConfig: ValidationConfig<ValidatableT, FailureT?>,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): Optional<FailureT> = failFast(validationConfig, throwableMapper)(validatable)

  // --- ERROR ACCUMULATION ---
  /**
   * Applies the Simple validators on a Single validatable in error-accumulation mode.
   *
   * @param validatable
   * @param validators
   * @param none Value to be returned in case of no failures.
   * @param <FailureT>
   * @param <ValidatableT>
   * @return List of Validation failures. </ValidatableT></FailureT>
   */
  @JvmStatic
  @JvmOverloads
  fun <FailureT, ValidatableT> validateAndAccumulateErrors(
    validatable: ValidatableT,
    validators: Collection<Validator<ValidatableT?, FailureT?>>,
    none: FailureT,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): List<FailureT?> =
    validateAndAccumulateErrors(validatable, liftAllToEtr(validators, none), none, throwableMapper)

  /**
   * Applies the validators on a Single validatable in error-accumulation mode. The Accumulated
   *
   * @param validatable
   * @param validators
   * @param none Value to be returned in case of no failures.
   * @param <FailureT>
   * @param <ValidatableT>
   * @param throwableMapper Function to map throwable to Failure in case of exception
   * @return List of Validation failures. EmptyList if all the validations pass.
   *   </ValidatableT></FailureT>
   */
  @JvmStatic
  @JvmOverloads
  fun <FailureT, ValidatableT> validateAndAccumulateErrors(
    validatable: ValidatableT,
    validators: List<ValidatorEtr<ValidatableT?, FailureT?>>,
    none: FailureT,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): List<FailureT?> {
    val results =
      accumulationStrategy(validators, throwableMapper)(validatable).map { result ->
        result.fold({ it }, { none })
      }
    return if (results.all { it == none }) emptyList() else results
  }
}
