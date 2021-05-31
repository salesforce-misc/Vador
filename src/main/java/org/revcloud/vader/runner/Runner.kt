@file:JvmName("Runner")

package org.revcloud.vader.runner

import org.revcloud.vader.lift.liftAllToEtr
import org.revcloud.vader.types.validators.Validator
import org.revcloud.vader.types.validators.ValidatorEtr
import java.util.Optional

fun <FailureT, ValidatableT> validateAndFailFastForHeader(
  validatable: ValidatableT,
  headerValidationConfig: HeaderValidationConfig<ValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): Optional<FailureT> = failFastForHeader(headerValidationConfig, throwableMapper)(validatable)

/**
 * This deals with Batch of Headers. This is placed here instead of Batch, so that consumers can
 * fluently use both these overloads.
 */
fun <FailureT, ValidatableT> validateAndFailFastForHeader(
  batchValidatable: Collection<ValidatableT>,
  headerValidationConfig: HeaderValidationConfig<ValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): Optional<FailureT> = batchValidatable
  .map { validatable: ValidatableT ->
    validateAndFailFastForHeader(validatable, headerValidationConfig, throwableMapper)
  }.firstOrNull { it.isPresent } ?: Optional.empty()

fun <FailureT, ValidatableT> validateAndFailFastForEach(
  validatable: ValidatableT,
  validationConfig: ValidationConfig<ValidatableT, FailureT>,
  throwableMapper: (Throwable) -> FailureT?
): Optional<FailureT> = failFast(validationConfig, throwableMapper)(validatable)

// --- ERROR ACCUMULATION ---
/**
 * Applies the Simple validators on a Single validatable in error-accumulation mode.
 *
 * @param validatable
 * @param validators
 * @param invalidValidatable FailureT if the validatable is null.
 * @param none               Value to be returned in case of no failure.
 * @param <FailureT>
 * @param <ValidatableT>
 * @return List of Validation failures.
</ValidatableT></FailureT> */
fun <FailureT, ValidatableT> validateAndAccumulateErrorsForValidators(
  validatable: ValidatableT,
  validators: Collection<Validator<ValidatableT?, FailureT?>>,
  none: FailureT,
  throwableMapper: (Throwable) -> FailureT?,
): List<FailureT?> =
  validateAndAccumulateErrors(validatable, liftAllToEtr(validators, none), none, throwableMapper)

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
  validators: List<ValidatorEtr<ValidatableT?, FailureT?>>,
  none: FailureT,
  throwableMapper: (Throwable) -> FailureT?,
): List<FailureT?> {
  val results = accumulationStrategy(validators, throwableMapper)(validatable)
    .map { result -> result.fold({ it }, { none }) }
  return if (results.all { it == none }) emptyList() else results
}
