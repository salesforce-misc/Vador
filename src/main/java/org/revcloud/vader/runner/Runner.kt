@file:JvmName("Runner")

package org.revcloud.vader.runner

import org.revcloud.vader.lift.liftAllToEtr
import org.revcloud.vader.types.validators.Validator
import org.revcloud.vader.types.validators.ValidatorEtr
import java.util.Optional

fun <FailureT, ContainerValidatableT> validateAndFailFastForContainer(
  container: ContainerValidatableT,
  containerValidationConfig: ContainerValidationConfig<ContainerValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): Optional<FailureT> = failFastForContainer(containerValidationConfig, throwableMapper)(container)

fun <FailureT, ContainerValidatableT, NestedContainerValidatableT> validateAndFailFastForContainer(
  container: ContainerValidatableT,
  containerValidationConfig: ContainerValidationConfigWithNested<ContainerValidatableT, NestedContainerValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): Optional<FailureT> = failFastForContainer(containerValidationConfig, throwableMapper)(container)

/**
 * This deals with Batch of Containers.
 * This is placed in this class instead of BatchRunner, so that consumers can fluently use both these overloads.
 */
fun <FailureT, ContainerValidatableT> validateAndFailFastForContainer(
  batchValidatable: Collection<ContainerValidatableT>,
  containerValidationConfig: ContainerValidationConfig<ContainerValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): Optional<FailureT> = batchValidatable.asSequence()
  .map { validatable: ContainerValidatableT ->
    validateAndFailFastForContainer(validatable, containerValidationConfig, throwableMapper)
  }.firstOrNull { it.isPresent } ?: Optional.empty()

fun <FailureT, ContainerValidatableT, NestedContainerValidatableT> validateAndFailFastForContainer(
  batchValidatable: Collection<ContainerValidatableT>,
  containerValidationConfig: ContainerValidationConfigWithNested<ContainerValidatableT, NestedContainerValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): Optional<FailureT> = batchValidatable.asSequence()
  .map { validatable: ContainerValidatableT ->
    validateAndFailFastForContainer(validatable, containerValidationConfig, throwableMapper)
  }.firstOrNull { it.isPresent } ?: Optional.empty()

fun <FailureT, ValidatableT> validateAndFailFast(
  validatable: ValidatableT,
  validationConfig: ValidationConfig<ValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): Optional<FailureT> = failFast(validationConfig, throwableMapper)(validatable)

// --- ERROR ACCUMULATION ---
/**
 * Applies the Simple validators on a Single validatable in error-accumulation mode.
 *
 * @param validatable
 * @param validators
 * @param invalidValidatable FailureT if the validatable is null.
 * @param none               Value to be returned in case of no failures.
 * @param <FailureT>
 * @param <ValidatableT>
 * @return List of Validation failures.
</ValidatableT></FailureT> */
fun <FailureT, ValidatableT> validateAndAccumulateErrors(
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
 * @param none               Value to be returned in case of no failures.
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
