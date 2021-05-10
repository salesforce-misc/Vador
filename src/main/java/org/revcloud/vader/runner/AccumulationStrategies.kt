@file:JvmName("AccumulationStrategies")
package org.revcloud.vader.runner

import io.vavr.Function1
import io.vavr.control.Either
import org.revcloud.vader.types.validators.Validator
import java.util.stream.Collectors

/**
 * Higher-order function to compose list of validators into Accumulation Strategy.
 *
 * @param validators
 * @param invalidValidatable
 * @param <FailureT>
 * @param <ValidatableT>
 * @return Composed Accumulation Strategy
 */
internal fun <FailureT, ValidatableT> accumulationStrategy(
    validators: List<Validator<ValidatableT, FailureT>>, invalidValidatable: FailureT,
    throwableMapper: Function1<Throwable, FailureT>
): Accumulation<ValidatableT, FailureT> = Accumulation { toBeValidated ->
    if (toBeValidated == null) listOf(
        Either.left(invalidValidatable)
    ) else fireValidators(
        Either.right(toBeValidated),
        validators.stream(),
        throwableMapper
    ).collect(Collectors.toList())
}

internal fun interface Accumulation<ValidatableT, FailureT> :
    Function1<ValidatableT, List<Either<FailureT, ValidatableT>>>
