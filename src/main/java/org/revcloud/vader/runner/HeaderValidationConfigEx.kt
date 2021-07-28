@file:JvmName("HeaderValidationConfigEx")

package org.revcloud.vader.runner

import de.cronn.reflection.util.PropertyUtils
import org.revcloud.vader.types.validators.ValidatorEtr

internal fun <HeaderValidatableT, FailureT> BaseHeaderValidationConfig<HeaderValidatableT?, FailureT?>.getHeaderValidatorsEx(): List<ValidatorEtr<HeaderValidatableT?, FailureT?>> =
  fromValidators1(withHeaderValidators) + fromValidators2(withHeaderValidator) + withHeaderValidatorEtrs

internal fun <HeaderValidatableT, FailureT> HeaderValidationConfig<HeaderValidatableT?, FailureT?>.getFieldNamesForBatchEx(
  validatableClazz: Class<HeaderValidatableT>
): Set<String> =
  withBatchMappers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()

internal fun <HeaderValidatableT, FailureT> HeaderValidationConfigWithNested<HeaderValidatableT?, *, FailureT?>.getFieldNamesForBatchEx(
  validatableClazz: Class<HeaderValidatableT>
): Set<String> =
  withBatchMappers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()

internal fun <NestedHeaderValidatableT, FailureT> HeaderValidationConfigWithNested<*, NestedHeaderValidatableT?, FailureT?>.getFieldNamesForNestedBatchEx(
  validatableClazz: Class<NestedHeaderValidatableT>
): Set<String> =
  withNestedBatchMappers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()
