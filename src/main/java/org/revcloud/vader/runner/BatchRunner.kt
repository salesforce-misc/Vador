@file:JvmName("BatchRunner")
package org.revcloud.vader.runner

import io.vavr.Function1
import io.vavr.Tuple
import io.vavr.Tuple2
import io.vavr.control.Either
import org.revcloud.vader.lift.liftAllSimple
import org.revcloud.vader.types.validators.SimpleValidator
import org.revcloud.vader.types.validators.Validator
import java.util.*
import java.util.stream.Collectors

fun <FailureT, ValidatableT> validateAndFailFast(
    validatables: List<ValidatableT>,
    invalidValidatable: FailureT,
    throwableMapper: Function1<Throwable, FailureT>,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT>
): List<Either<FailureT?, ValidatableT?>> =
    failFastForBatch(invalidValidatable, throwableMapper, batchValidationConfig)
        .apply(validatables)

fun <FailureT, ValidatableT, PairT> validateAndFailFast(
    validatables: List<ValidatableT>,
    invalidValidatable: FailureT,
    throwableMapper: Function1<Throwable, FailureT?>,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
    pairForInvalidMapper: Function1<ValidatableT, PairT>
): List<Either<Tuple2<PairT?, FailureT?>, ValidatableT?>> {
    val validationResults =
        failFastForBatch(invalidValidatable, throwableMapper, batchValidationConfig)
            .apply(validatables)
    return io.vavr.collection.List.ofAll(validationResults).zipWith(
        validatables
    ) { result, validatable ->
        result.mapLeft { failure ->
            Tuple.of(
                pairForInvalidMapper.apply(validatable),
                failure
            )
        }
    }.toJavaList()
}

fun <FailureT, ValidatableT> validateAndFailFastAllOrNone(
    validatables: List<ValidatableT>,
    invalidValidatable: FailureT,
    throwableMapper: Function1<Throwable, FailureT>,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT>
): Optional<FailureT> =
    failFastAllOrNoneForBatch(invalidValidatable, throwableMapper, batchValidationConfig)
        .apply(validatables)

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
    throwableMapper: Function1<Throwable, FailureT?>
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
    throwableMapper: Function1<Throwable, FailureT?>
): List<List<Either<FailureT?, ValidatableT?>>> = validatables.stream()
    .map(accumulationStrategy(validators, invalidValidatable, throwableMapper))
    .collect(Collectors.toList())
