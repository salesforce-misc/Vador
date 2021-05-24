@file:JvmName("BatchRunner")

package org.revcloud.vader.runner

import io.vavr.Tuple
import io.vavr.Tuple2
import io.vavr.control.Either
import org.revcloud.vader.lift.liftAllToEtr
import org.revcloud.vader.types.validators.Validator
import org.revcloud.vader.types.validators.ValidatorEtr
import java.util.Optional

fun <FailureT, ValidatableT> validateAndFailFast(
    validatables: List<ValidatableT>,
    nullValidatable: FailureT?,
    throwableMapper: (Throwable) -> FailureT?,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>
): List<Either<FailureT?, ValidatableT?>> =
    failFastForBatch(nullValidatable, throwableMapper, batchValidationConfig)(validatables)

fun <FailureT, ValidatableT, PairT> validateAndFailFast(
    validatables: List<ValidatableT>,
    invalidValidatable: FailureT,
    throwableMapper: (Throwable) -> FailureT?,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
    pairForInvalidMapper: (ValidatableT) -> PairT?
): List<Either<Tuple2<PairT?, FailureT?>, ValidatableT?>> {
    val orderedValidationResults =
        validateAndFailFast(
            validatables,
            invalidValidatable,
            throwableMapper,
            batchValidationConfig
        )
    return orderedValidationResults.zip(validatables)
        .map { (result, validatable) ->
            result.mapLeft {
                Tuple.of(
                    pairForInvalidMapper(validatable),
                    it
                )
            }
        }
}

fun <FailureT, ValidatableT> validateAndFailFastAllOrNone(
    validatables: List<ValidatableT>,
    invalidValidatable: FailureT,
    throwableMapper: (Throwable) -> FailureT?,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>
): Optional<FailureT> =
    failFastAllOrNoneForBatch(invalidValidatable, throwableMapper, batchValidationConfig)(
        validatables
    )

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
): List<List<Either<FailureT?, ValidatableT?>>> = validateAndAccumulateErrors(
    validatables,
    liftAllToEtr(validators, none),
    throwableMapper
)

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
