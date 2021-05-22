@file:JvmName("HeaderValidationConfigEx")

package org.revcloud.vader.runner

import de.cronn.reflection.util.PropertyUtils
import org.revcloud.vader.types.validators.Validator

internal fun <HeaderValidatableT, FailureT> HeaderValidationConfig<HeaderValidatableT?, FailureT?>.getHeaderValidatorsEx(): List<Validator<HeaderValidatableT?, FailureT?>> =
    fromSimpleValidators1(withSimpleHeaderValidators) +
            fromSimpleValidators2(withSimpleHeaderValidator) + withHeaderValidators

internal fun <HeaderValidatableT, FailureT> HeaderValidationConfig<HeaderValidatableT?, FailureT?>.getFieldNamesForBatchEx(
    validatableClazz: Class<HeaderValidatableT>
): Set<String> =
    withBatchMappers.map { PropertyUtils.getPropertyName(validatableClazz, it) }.toSet()
