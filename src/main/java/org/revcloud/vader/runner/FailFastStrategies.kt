@file:JvmName("FailFastStrategies")

package org.revcloud.vader.runner

import io.vavr.Function1
import io.vavr.control.Either
import java.util.*
import java.util.function.Function

internal fun interface FailFast<ValidatableT, FailureT> :
    Function1<ValidatableT, Either<FailureT, ValidatableT>>

internal fun interface FailFastForBatch<ValidatableT, FailureT> :
    Function1<List<ValidatableT>, List<Either<FailureT, ValidatableT>>>

internal fun interface FailFastAllOrNoneForBatch<ValidatableT, FailureT> :
    Function1<List<ValidatableT>, Optional<FailureT>>

internal fun interface FailFastStrategyForHeader<ValidatableT, FailureT> :
    Function1<ValidatableT, Optional<FailureT>>

/**
 * Config
 *
 * @param invalidValidatable
 * @param throwableMapper
 * @param validationConfig
 * @param <FailureT>
 * @param <ValidatableT>
 * @return
</ValidatableT></FailureT> */
internal fun <FailureT, ValidatableT> failFast(
    throwableMapper: Function1<Throwable, FailureT>,
    validationConfig: ValidationConfig<ValidatableT, FailureT>
): FailFast<ValidatableT, FailureT> =
    FailFast { validatable: ValidatableT ->
        findFirstFailure(
            Either.right(validatable),
            validationConfig,
            throwableMapper
        )
    }

/**
 * Batch + Simple + Config
 *
 * @param invalidValidatable
 * @param throwableMapper
 * @param validationConfig
 * @param <FailureT>
 * @param <ValidatableT>
 * @return
</ValidatableT></FailureT> */
internal fun <FailureT, ValidatableT> failFastForBatch(
    invalidValidatable: FailureT,
    throwableMapper: Function1<Throwable, FailureT>,
    validationConfig: BatchValidationConfig<ValidatableT, FailureT>
): FailFastForBatch<ValidatableT, FailureT> = FailFastForBatch { validatables ->
    val filteredValidatables = filterInvalidatablesAndDuplicates(
        validatables,
        invalidValidatable,
        validationConfig
    )
    filteredValidatables
        .map { validatable ->
            findFirstFailure(
                validatable,
                validationConfig,
                throwableMapper
            )
        }.toJavaList()
}

internal fun <FailureT, ValidatableT> failFastAllOrNoneForBatch(
    invalidValidatable: FailureT,
    throwableMapper: Function1<Throwable, FailureT>,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT>
): FailFastAllOrNoneForBatch<ValidatableT, FailureT> = FailFastAllOrNoneForBatch { validatables ->
    filterInvalidatablesAndDuplicatesForAllOrNone(
        validatables,
        invalidValidatable,
        batchValidationConfig
    ).or {
        validatables.stream()
            .map(Function<ValidatableT, Either<FailureT, ValidatableT>> { right: ValidatableT ->
                Either.right(
                    right
                )
            }).map { validatable: Either<FailureT, ValidatableT> ->
                findFirstFailure(
                    validatable,
                    batchValidationConfig,
                    throwableMapper
                )
            }
            .filter { it.isLeft }
            .findFirst().map { it.left }
    }
}

internal fun <FailureT, ValidatableT> failFastForHeader(
    throwableMapper: Function1<Throwable, FailureT>,
    validationConfig: HeaderValidationConfig<ValidatableT, FailureT>
): FailFastStrategyForHeader<ValidatableT, FailureT> = FailFastStrategyForHeader { validatable: ValidatableT ->
    val batch: List<*> = validationConfig.withBatchMappers.mapNotNull { it[validatable] }.flatten()
    validateSize(batch, validationConfig).or {
        fireValidators(
            Either.right(validatable),
            validationConfig.getHeaderValidatorsStream(),
            throwableMapper
        ).filter { it.isLeft }
            .findFirst()
            .map { it.swap() }
            .flatMap { it.toJavaOptional() }
    }
}
