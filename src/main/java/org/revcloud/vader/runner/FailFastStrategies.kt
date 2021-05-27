package org.revcloud.vader.runner

import io.vavr.control.Either
import io.vavr.kotlin.right
import java.util.Optional

internal typealias FailFast<ValidatableT, FailureT> = (ValidatableT) -> Either<FailureT?, ValidatableT?>

internal typealias FailFastForBatch<ValidatableT, FailureT> = (List<ValidatableT>) -> List<Either<FailureT?, ValidatableT?>>

internal typealias FailFastAllOrNoneForBatch<ValidatableT, FailureT> = (List<ValidatableT>) -> Optional<FailureT>

internal typealias FailFastForHeader<ValidatableT, FailureT> = (ValidatableT) -> Optional<FailureT>

/**
 * Config
 *
 * @param invalidValidatable
 * @param throwableMapper
 * @param validationConfig
 * @param <FailureT>
 * @param <ValidatableT>
 * @return
 */
@JvmSynthetic
internal fun <FailureT, ValidatableT> failFast(
    throwableMapper: (Throwable) -> FailureT?,
    validationConfig: ValidationConfig<ValidatableT, FailureT>
): FailFast<ValidatableT, FailureT> =
    { findFirstFailure(right(it), validationConfig, throwableMapper) }

/**
 * Batch + Simple + Config
 *
 * @param nullValidatable
 * @param throwableMapper
 * @param validationConfig
 * @param <FailureT>
 * @param <ValidatableT>
 * @return
</ValidatableT></FailureT> */
@JvmSynthetic
internal fun <FailureT, ValidatableT> failFastForBatch(
    nullValidatable: FailureT?,
    throwableMapper: (Throwable) -> FailureT?,
    validationConfig: BatchValidationConfig<ValidatableT, FailureT?>
): FailFastForBatch<ValidatableT, FailureT> = { validatables ->
    val filteredValidatables =
        filterNullValidatablesAndDuplicates(validatables, nullValidatable, validationConfig)
    filteredValidatables.map { findFirstFailure(it, validationConfig, throwableMapper) }
}

// TODO 13/05/21 gopala.akshintala: Reconsider any advantage of having this as a HOF 
@JvmSynthetic
internal fun <FailureT, ValidatableT> failFastAllOrNoneForBatch(
    invalidValidatable: FailureT,
    throwableMapper: (Throwable) -> FailureT?,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>
): FailFastAllOrNoneForBatch<ValidatableT, FailureT> = { validatables ->
    filterNullValidatablesAndDuplicatesForAllOrNone(
        validatables,
        invalidValidatable,
        batchValidationConfig
    ).or {
        validatables.map { findFirstFailure(right(it), batchValidationConfig, throwableMapper) }
            .firstOrNull { it.isLeft }?.swap()?.toJavaOptional() ?: Optional.empty()
    }
}

@JvmSynthetic
internal fun <FailureT, ValidatableT> failFastForHeader(
    throwableMapper: (Throwable) -> FailureT?,
    validationConfig: HeaderValidationConfig<ValidatableT, FailureT>
): FailFastForHeader<ValidatableT, FailureT> = { validatable: ValidatableT ->
    val batch: List<*> = validationConfig.withBatchMappers.mapNotNull { it[validatable] }.flatten()
    validateBatchSize(batch, validationConfig).or {
        fireValidators(right(validatable), validationConfig.headerValidators, throwableMapper)
            .firstOrNull { it.isLeft }?.swap()?.toJavaOptional() ?: Optional.empty()
    }
}
