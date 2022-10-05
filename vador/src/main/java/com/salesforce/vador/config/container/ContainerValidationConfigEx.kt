/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

@file:JvmName("ContainerValidationConfigEx")

package com.salesforce.vador.config.container

import com.salesforce.vador.config.base.BaseContainerValidationConfig
import com.salesforce.vador.execution.strategies.util.fromValidators1
import com.salesforce.vador.execution.strategies.util.fromValidators2
import com.salesforce.vador.types.ValidatorEtr
import de.cronn.reflection.util.PropertyUtils

internal fun <ContainerValidatableT, FailureT> _root_ide_package_.com.salesforce.vador.config.base.BaseContainerValidationConfig<ContainerValidatableT?, FailureT?>.getContainerValidatorsEx(): List<ValidatorEtr<ContainerValidatableT?, FailureT?>> =
  fromValidators1(withContainerValidators) + fromValidators2(withContainerValidator) + withContainerValidatorEtrs

internal fun <ContainerValidatableT, FailureT> _root_ide_package_.com.salesforce.vador.config.container.ContainerValidationConfig<ContainerValidatableT?, FailureT?>.getFieldNamesForBatchEx(
  validatableClazz: Class<ContainerValidatableT>
): Set<String> =
  withBatchMembers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()

internal fun <ContainerValidatableT, FailureT> _root_ide_package_.com.salesforce.vador.config.container.ContainerValidationConfigWith2Levels<ContainerValidatableT?, *, FailureT?>.getFieldNamesForBatchEx(
  validatableClazz: Class<ContainerValidatableT>
): Set<String> =
  withBatchMembers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()

internal fun <NestedContainerValidatableT, FailureT> _root_ide_package_.com.salesforce.vador.config.container.ContainerValidationConfigWith2Levels<*, NestedContainerValidatableT?, FailureT?>.getFieldNamesForBatchLevel1Ex(
  validatableClazz: Class<NestedContainerValidatableT>
): Set<String> =
  withScopeOf1LevelDeep.withBatchMembers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()
