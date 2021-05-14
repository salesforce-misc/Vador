@file:JvmName("Runner")

package org.revcloud.vader.runner

import org.revcloud.vader.lift.liftAllSimple
import org.revcloud.vader.types.validators.SimpleValidator
import org.revcloud.vader.types.validators.Validator
import java.util.*

fun <FailureT, ValidatableT> validateAndFailFastForHeader(
    validatable: ValidatableT,
    throwableMapper: (Throwable) -> FailureT?,
    validationConfig: HeaderValidationConfig<ValidatableT, FailureT?>
): Optional<FailureT?> = failFastForHeader(throwableMapper, validationConfig)(validatable)

fun <FailureT, ValidatableT> validateAndFailFast(
    validatable: ValidatableT,
    throwableMapper: (Throwable) -> FailureT?,
    validationConfig: ValidationConfig<ValidatableT, FailureT>
): Optional<FailureT?> = failFast(throwableMapper, validationConfig)(validatable)
    .swap().toJavaOptional()

// --- ERROR ACCUMULATION ---
/**
 * Applies the Simple validators on a Single validatable in error-accumulation mode.
 *
 * @param validatable
 * @param simpleValidators
 * @param invalidValidatable FailureT if the validatable is null.
 * @param none               Value to be returned in case of no failure.
 * @param <FailureT>
 * @param <ValidatableT>
 * @return List of Validation failures.
</ValidatableT></FailureT> */
fun <FailureT, ValidatableT> validateAndAccumulateErrorsForSimpleValidators(
    validatable: ValidatableT,
    simpleValidators: Collection<SimpleValidator<ValidatableT?, FailureT?>>,
    invalidValidatable: FailureT,
    none: FailureT,
    throwableMapper: (Throwable) -> FailureT?,
): List<FailureT?> = validateAndAccumulateErrors(
    validatable,
    liftAllSimple(simpleValidators, none),
    invalidValidatable,
    none,
    throwableMapper
)

/**
 * Applies the validators on a Single validatable in error-accumulation mode. The Accumulated
 *
 * @param validatable
 * @param validators
 * @param invalidValidatable FailureT if the validatable is null.
 * @param none               Value to be returned in case of no failure.
 * @param <FailureT>
 * @param <ValidatableT>
 * @param throwableMapper   Function to map throwable to Failure in case of exception
 * @return List of Validation failures. EmptyList if all the validations pass.
</ValidatableT></FailureT> */
fun <FailureT, ValidatableT> validateAndAccumulateErrors(
    validatable: ValidatableT,
    validators: List<Validator<ValidatableT?, FailureT?>>,
    invalidValidatable: FailureT,
    none: FailureT,
    throwableMapper: (Throwable) -> FailureT?,
): List<FailureT?> {
    val results = accumulationStrategy(validators, invalidValidatable, throwableMapper)(validatable)
        .map { result -> result.fold({it}, {none}) }
    return if (results.all { it == none }) emptyList() else results
}
