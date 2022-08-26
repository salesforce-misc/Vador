/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

@file:JvmName("ContainerValidationConfigEx")

package org.revcloud.vader.config.container

import de.cronn.reflection.util.PropertyUtils
import org.revcloud.vader.config.base.BaseContainerValidationConfig
import org.revcloud.vader.execution.strategies.util.fromValidators1
import org.revcloud.vader.execution.strategies.util.fromValidators2
import org.revcloud.vader.types.ValidatorEtr

internal fun <ContainerValidatableT, FailureT> BaseContainerValidationConfig<ContainerValidatableT?, FailureT?>.getContainerValidatorsEx(): List<ValidatorEtr<ContainerValidatableT?, FailureT?>> =
  fromValidators1(withContainerValidators) + fromValidators2(withContainerValidator) + withContainerValidatorEtrs

internal fun <ContainerValidatableT, FailureT> ContainerValidationConfig<ContainerValidatableT?, FailureT?>.getFieldNamesForBatchEx(
  validatableClazz: Class<ContainerValidatableT>
): Set<String> =
  withBatchMembers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()

internal fun <ContainerValidatableT, FailureT> ContainerValidationConfigWith2Levels<ContainerValidatableT?, *, FailureT?>.getFieldNamesForBatchEx(
  validatableClazz: Class<ContainerValidatableT>
): Set<String> =
  withBatchMembers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()

internal fun <NestedContainerValidatableT, FailureT> ContainerValidationConfigWith2Levels<*, NestedContainerValidatableT?, FailureT?>.getFieldNamesForBatchLevel1Ex(
  validatableClazz: Class<NestedContainerValidatableT>
): Set<String> =
  withScopeOf1LevelDeep.withBatchMembers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()
