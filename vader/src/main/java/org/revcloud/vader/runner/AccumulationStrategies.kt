package org.revcloud.vader.runner

import io.vavr.control.Either
import io.vavr.kotlin.right
import org.revcloud.vader.types.ValidatorEtr

/**
 * Higher-order function to compose list of validators into Accumulation Strategy.
 *
 * @param validators
 * @param invalidValidatable
 * @param <FailureT>
 * @param <ValidatableT>
 * @return Composed Accumulation Strategy
 */
@JvmSynthetic
internal fun <FailureT, ValidatableT> accumulationStrategy(
  validators: List<ValidatorEtr<ValidatableT?, FailureT?>>,
  throwableMapper: (Throwable) -> FailureT?
): Accumulation<ValidatableT, FailureT> = {
  fireValidators(right(it), validators, throwableMapper).toList()
}

internal typealias Accumulation <ValidatableT, FailureT> = (ValidatableT) -> List<Either<FailureT?, ValidatableT?>>
