@file:JvmName("BatchRunner")

package org.revcloud.vader.runner

import io.vavr.Tuple
import io.vavr.Tuple2
import io.vavr.control.Either
import lombok.NonNull
import org.revcloud.vader.lift.liftAllSimple
import org.revcloud.vader.types.validators.SimpleValidator
import org.revcloud.vader.types.validators.Validator
import java.util.*

fun <FailureT, ValidatableT> validateAndFailFast(
    validatables: List<@NonNull ValidatableT>,
    nullValidatable: FailureT?,
    throwableMapper: (Throwable) -> FailureT?,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>
): List<Either<FailureT?, ValidatableT?>> =
    failFastForBatch(nullValidatable, throwableMapper, batchValidationConfig)(validatables)

fun <FailureT, ValidatableT, PairT> validateAndFailFast(
    validatables: List<@NonNull ValidatableT>,
    invalidValidatable: FailureT,
    throwableMapper: (Throwable) -> FailureT?,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
    pairForInvalidMapper: (ValidatableT) -> PairT?
): List<Either<Tuple2<PairT?, FailureT?>, ValidatableT?>> {
    val validationResults =
        validateAndFailFast(validatables, invalidValidatable, throwableMapper, batchValidationConfig)
    return validationResults.zip(validatables)
        .map { (result, validatable) -> result.mapLeft { Tuple.of(pairForInvalidMapper(validatable), it) } }
}

fun <FailureT, ValidatableT> validateAndFailFastAllOrNone(
    validatables: List<ValidatableT>,
    invalidValidatable: FailureT,
    throwableMapper: (Throwable) -> FailureT?,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>
): Optional<FailureT> =
    failFastAllOrNoneForBatch(invalidValidatable, throwableMapper, batchValidationConfig)(validatables)

// --- ERROR ACCUMULATION ---
/**
 * Validates a list of validatables against a list of Simple validations, in error-accumulation mode, per validatable.
 *
 * @param validatables
 * @param simpleValidators
 * @param invalidValidatable FailureT if the validatable is null.
 * @param <FailureT>
 * @param <ValidatableT>
 * @return List of Validation failures.
</ValidatableT></FailureT> */
fun <FailureT, ValidatableT> validateAndAccumulateErrors(
    validatables: List<ValidatableT>,
    simpleValidators: List<SimpleValidator<ValidatableT?, FailureT?>>,
    invalidValidatable: FailureT,
    none: FailureT,
    throwableMapper: (Throwable) -> FailureT?,
): List<List<Either<FailureT?, ValidatableT?>>> = validateAndAccumulateErrors(
    validatables,
    liftAllSimple(simpleValidators, none),
    invalidValidatable,
    throwableMapper
)

/**
 * Validates a list of validatables against a list of validations, in error-accumulation mode, per validatable.
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
    invalidValidatable: FailureT,
    throwableMapper: (Throwable) -> FailureT?,
): List<List<Either<FailureT?, ValidatableT?>>> =
    validatables.map(accumulationStrategy(validators, invalidValidatable, throwableMapper))
