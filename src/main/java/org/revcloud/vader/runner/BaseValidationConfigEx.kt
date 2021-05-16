@file:JvmName("BaseValidationConfigEx")

package org.revcloud.vader.runner

import de.cronn.reflection.util.PropertyUtils
import org.revcloud.vader.types.validators.Validator
import java.util.Optional
import java.util.function.Predicate

internal fun <ValidatableT, FailureT> BaseValidationConfig<ValidatableT, FailureT>.getValidators(): List<Validator<ValidatableT?, FailureT?>> =
    fromSimpleValidators1(withSimpleValidators) + fromSimpleValidators2(withSimpleValidator) + withValidators

internal fun <ValidatableT, FailureT> BaseValidationConfig<ValidatableT, FailureT>.getSpecWithNameEx(
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
    (shouldHaveFieldsOrFailWith.keys + (shouldHaveFieldsOrFailWithFn?._1 ?: emptyList()))
        .map { PropertyUtils.getPropertyName(beanClass, it) }.toSet()

internal fun <ValidatableT> BaseValidationConfig<ValidatableT, *>.getRequiredFieldNamesForSFIdFormatEx(
    beanClass: Class<ValidatableT>
): Set<String> =
    (
        shouldHaveValidSFIdFormatOrFailWith.keys + (
            shouldHaveValidSFIdFormatOrFailWithFn?._1
                ?: emptyList()
            )
        )
        .map { PropertyUtils.getPropertyName(beanClass, it) }.toSet()

internal fun <ValidatableT> BaseValidationConfig<ValidatableT, *>.getNonRequiredFieldNamesForSFIdFormatEx(
    beanClass: Class<ValidatableT>
): Set<String> =
    (
        absentOrHaveValidSFIdFieldsOrFailWith.keys + (
            absentOrHaveValidSFIdFormatOrFailWithFn?._1
                ?: emptyList()
            )
        )
        .map { PropertyUtils.getPropertyName(beanClass, it) }.toSet()
