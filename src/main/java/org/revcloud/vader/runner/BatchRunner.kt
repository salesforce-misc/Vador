@file:JvmName("BatchRunner")

package org.revcloud.vader.runner

import io.vavr.Tuple
import io.vavr.Tuple2
import io.vavr.control.Either
import org.revcloud.vader.lift.liftAllToEtr
import org.revcloud.vader.types.failure.FFBatchOfBatchFailure
import org.revcloud.vader.types.validators.Validator
import org.revcloud.vader.types.validators.ValidatorEtr
import java.util.Optional

fun <FailureT, ValidatableT> validateAndFailFastForEach(
  validatables: List<ValidatableT>,
  batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
  nullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?
): List<Either<FailureT?, ValidatableT?>> =
  failFastForEach(batchValidationConfig, nullValidatable, throwableMapper)(validatables)

fun <FailureT, ContainerValidatableT, MemberValidatableT> validateAndFailFastForEach(
  validatables: List<ContainerValidatableT>,
  batchOfBatch1ValidationConfig: BatchOfBatch1ValidationConfig<ContainerValidatableT, MemberValidatableT, FailureT?>,
  nullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?
): List<Either<FFBatchOfBatchFailure<FailureT?>, ContainerValidatableT?>> =
  failFastForEach(batchOfBatch1ValidationConfig, nullValidatable, throwableMapper)(validatables)

fun <FailureT, ValidatableT, PairT> validateAndFailFastForEach(
  validatables: List<ValidatableT>,
  batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
  pairForInvalidMapper: (ValidatableT) -> PairT?,
  nullValidatable: FailureT,
  throwableMapper: (Throwable) -> FailureT?
): List<Either<Tuple2<PairT?, FailureT?>, ValidatableT?>> {
  val orderedValidationResults =
    validateAndFailFastForEach(
      validatables,
      batchValidationConfig,
      nullValidatable,
      throwableMapper
    )
  return orderedValidationResults.zip(validatables)
    .map { (result, validatable) ->
      result.mapLeft { Tuple.of(pairForInvalidMapper(validatable), it) }
    }
}

fun <FailureT, ValidatableT> validateAndFailFastForAny(
  validatables: List<ValidatableT>,
  batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
  nullValidatable: FailureT,
  throwableMapper: (Throwable) -> FailureT?
): Optional<FailureT> =
  failFastForAny(nullValidatable, batchValidationConfig, throwableMapper)(validatables)

// --- ERROR ACCUMULATION ---
// TODO 20/05/21 gopala.akshintala: ValidationConfig integration
/**
 * Validates a list of validatables against a list of Simple validations, in error-accumulation mode, per validatable.
 *
 * @param validatables
 * @param validators
 * @param invalidValidatable FailureT if the validatable is null.
 * @param <FailureT>
 * @param <ValidatableT>
 * @return List of Validation failures.
</ValidatableT></FailureT> */
fun <FailureT, ValidatableT> validateAndAccumulateErrors(
  validatables: List<ValidatableT>,
  validators: List<Validator<ValidatableT?, FailureT?>>,
  none: FailureT, // TODO 20/05/21 gopala.akshintala: Check on adding InvalidatableT
  throwableMapper: (Throwable) -> FailureT?,
): List<List<Either<FailureT?, ValidatableT?>>> =
  validateAndAccumulateErrors(validatables, liftAllToEtr(validators, none), throwableMapper)

/**
 * Validates a list of validatables against a list of validations, in error-accumulation mode, per validatable.
 *
 * @param validatables
 * @param validatorEtrs
 * @param invalidValidatable FailureT if the validatable is null.
 * @param <FailureT>
 * @param <ValidatableT>
 * @return List of Validation failures.
</ValidatableT></FailureT> */
fun <FailureT, ValidatableT> validateAndAccumulateErrors(
  validatables: List<ValidatableT>,
  validatorEtrs: List<ValidatorEtr<ValidatableT?, FailureT?>>,
  throwableMapper: (Throwable) -> FailureT?,
): List<List<Either<FailureT?, ValidatableT?>>> =
  validatables.map(accumulationStrategy(validatorEtrs, throwableMapper))
