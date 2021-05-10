package org.revcloud.vader.runner

import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function1
import io.vavr.control.Either
import java.util.*
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors
import java.util.stream.Stream


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
        Utils.findFirstFailure(
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
): FailFastStrategies.FailFastForBatch<ValidatableT, FailureT> {
    return FailFastStrategies.FailFastForBatch { validatables ->
        val filteredValidatables = Utils.filterInvalidatablesAndDuplicates(
            validatables,
            invalidValidatable,
            validationConfig
        )
        filteredValidatables
            .map { validatable ->
                Utils.findFirstFailure(
                    validatable,
                    validationConfig,
                    throwableMapper
                )
            }.toJavaList()
    }
}

internal fun <FailureT, ValidatableT> failFastAllOrNoneForBatch(
    invalidValidatable: FailureT,
    throwableMapper: Function1<Throwable, FailureT>,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT>
): FailFastStrategies.FailFastAllOrNoneForBatch<ValidatableT, FailureT> {
    return FailFastStrategies.FailFastAllOrNoneForBatch { validatables ->
        Utils.filterInvalidatablesAndDuplicatesForAllOrNone(
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
                    Utils.findFirstFailure(
                        validatable,
                        batchValidationConfig,
                        throwableMapper
                    )
                }
                .filter { it.isLeft }
                .findFirst().map { it.left }
        }
    }
}

internal fun <FailureT, ValidatableT> failFastForHeader(
    throwableMapper: Function1<Throwable, FailureT>,
    validationConfig: HeaderValidationConfig<ValidatableT, FailureT>
): FailFastStrategies.FailFastStrategyForHeader<ValidatableT, FailureT> {
    return FailFastStrategies.FailFastStrategyForHeader { validatable: ValidatableT ->
        val batch: List<*> = validationConfig.withBatchMappers.mapNotNull { it[validatable] }
        Utils.validateSize(batch, validationConfig).or {
                Utils.fireValidators(
                    Either.right(validatable),
                    validationConfig.headerValidatorsStream,
                    throwableMapper)
                    .filter { it.isLeft }
                    .findFirst()
                    .map { it.swap() }
                    .flatMap { it.toJavaOptional() }
            }
    }
}
