/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
@file:JvmName("ContainerValidationConfigEx")

package com.salesforce.vador.config.container

import com.salesforce.vador.config.base.BaseContainerValidationConfig
import com.salesforce.vador.execution.strategies.util.fromValidators1
import com.salesforce.vador.execution.strategies.util.fromValidators2
import com.salesforce.vador.types.ValidatorEtr
import de.cronn.reflection.util.PropertyUtils

@Suppress("UNCHECKED_CAST")
internal fun <ContainerValidatableT, FailureT> BaseContainerValidationConfig<
  ContainerValidatableT,
  FailureT,
>
  .getContainerValidatorsEx(): List<ValidatorEtr<ContainerValidatableT, FailureT?>> {
  val executionView = this as BaseContainerValidationConfig<ContainerValidatableT?, FailureT?>
  return (fromValidators1(executionView.withContainerValidators) +
    fromValidators2(executionView.withContainerValidator) +
    executionView.withContainerValidatorEtrs)
    as List<ValidatorEtr<ContainerValidatableT, FailureT?>>
}

internal fun <ContainerValidatableT, FailureT> BaseSingleLevelContainerValidationConfig<
  ContainerValidatableT,
  FailureT,
>
  .getFieldNamesForBatchEx(validatableClazz: Class<ContainerValidatableT>): Set<String> =
  withBatchMembers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()

internal fun <ContainerValidatableT, FailureT> AbstractContainerValidationConfigWith2Levels<
  ContainerValidatableT,
  *,
  FailureT,
>
  .getFieldNamesForBatchEx(validatableClazz: Class<ContainerValidatableT>): Set<String> =
  withBatchMembers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()

internal fun <NestedContainerValidatableT, FailureT> AbstractContainerValidationConfigWith2Levels<
  *,
  NestedContainerValidatableT,
  FailureT,
>
  .getFieldNamesForBatchLevel1Ex(
  validatableClazz: Class<NestedContainerValidatableT>
): Set<String> =
  withScopeOf1LevelDeep.withBatchMembers
    .map { PropertyUtils.getPropertyName(validatableClazz, it) }
    .toSet()
