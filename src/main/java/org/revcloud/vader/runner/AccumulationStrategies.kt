@file:JvmName("AccumulationStrategies")

package org.revcloud.vader.runner

import io.vavr.control.Either
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import org.revcloud.vader.types.validators.Validator

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
    validators: List<Validator<ValidatableT?, FailureT?>>,
    invalidValidatable: FailureT,
    throwableMapper: (Throwable) -> FailureT?,
): Accumulation<ValidatableT, FailureT> = {
    if (it == null) listOf(left(invalidValidatable))
    else fireValidators(right(it), validators, throwableMapper)
}

typealias Accumulation<ValidatableT, FailureT> = (ValidatableT) -> List<Either<FailureT?, ValidatableT?>>
