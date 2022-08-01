/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

@file:JvmName("BaseValidationConfigEx")

package org.revcloud.vader.runner.config

import de.cronn.reflection.util.PropertyUtils
import org.revcloud.vader.runner.fromValidators1
import org.revcloud.vader.runner.fromValidators2
import org.revcloud.vader.specs.factory.SpecFactory
import org.revcloud.vader.specs.specs.BaseSpec
import org.revcloud.vader.types.ValidatorEtr
import java.util.Optional
import java.util.function.Predicate

internal fun <ValidatableT, FailureT> BaseValidationConfig<ValidatableT, FailureT>.getValidators(): List<ValidatorEtr<ValidatableT?, FailureT?>> =
  fromValidators1(withValidators) + fromValidators2(withValidator) + withValidatorEtrs

internal fun <ValidatableT, FailureT> BaseValidationConfig<ValidatableT, FailureT>.getSpecsEx(): List<BaseSpec<ValidatableT, FailureT>> {
  val specFactory = SpecFactory<ValidatableT, FailureT?>()
  return (specify?.invoke(specFactory)?.map { it.done() as BaseSpec<ValidatableT, FailureT> } ?: emptyList()) +
    withSpecs.map { it.invoke(specFactory).done() as BaseSpec<ValidatableT, FailureT> }
}

internal fun <ValidatableT, FailureT> BaseValidationConfig<ValidatableT, FailureT>.getPredicateOfSpecForTestEx(
  nameForTest: String
): Optional<Predicate<ValidatableT?>> {
  // TODO 29/04/21 gopala.akshintala: Move this duplicate-check to ValidationConfig `prepare`
  val specNameToSpecs =
    specs.groupingBy { it.nameForTest }.eachCount().filter { it.value > 1 }.keys.filterNotNull()
  if (specNameToSpecs.isNotEmpty()) {
    throw IllegalArgumentException("Specs with Duplicate NamesForTest found: $specNameToSpecs")
  }
  return Optional.ofNullable(specs.first { it.nameForTest == nameForTest }?.toPredicate())
}

internal fun <ValidatableT> BaseValidationConfig<ValidatableT, *>.getRequiredFieldNamesEx(beanClass: Class<ValidatableT>): Set<String> =
  (shouldHaveFieldsOrFailWith.keys + (shouldHaveFieldsOrFailWithFn?._1 ?: emptyList()) + shouldHaveFieldOrFailWithFn.keys)
    .map { PropertyUtils.getPropertyName(beanClass, it) }.toSet()
