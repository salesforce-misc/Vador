@file:JvmName("ValidatorLiftUtil")

package org.revcloud.vader.lift

import io.vavr.control.Either
import org.revcloud.vader.types.validators.SimpleValidator
import org.revcloud.vader.types.validators.Validator

/**
 * Lifts Simple Validator to Validator type.
 *
 * @param toBeLifted     Simple validator to be lifted
 * @param none           Value to be returned in case of no failure
 * @param <FailureT>
 * @param <ValidatableT>
 * @return Validator
</ValidatableT></FailureT> */
fun <FailureT, ValidatableT> liftSimple(
    toBeLifted: SimpleValidator<ValidatableT, FailureT>, none: FailureT
): Validator<ValidatableT, FailureT> = Validator { validatable ->
    validatable.flatMap { toBeValidated: ValidatableT ->
        val result = toBeLifted.unchecked().apply(toBeValidated)
        if (result == none) validatable else Either.left(result)
    }
}

/**
 * Lifts a list of Simple validators to list of Validator type.
 *
 * @param toBeLiftedFns  List of Simple functions to be lifted.
 * @param none           Value to be returned in case of no failure.
 * @param <FailureT>
 * @param <ValidatableT>
 * @return List of Validators
</ValidatableT></FailureT> */
fun <FailureT, ValidatableT> liftAllSimple(
    toBeLiftedFns: Collection<SimpleValidator<ValidatableT, FailureT>>,
    none: FailureT
): List<Validator<ValidatableT, FailureT>> =
    toBeLiftedFns.map { liftSimple(it, none) }
