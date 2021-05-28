@file:JvmName("ValidatorLiftUtil")

package org.revcloud.vader.lift

import io.vavr.kotlin.left
import org.revcloud.vader.types.validators.Validator
import org.revcloud.vader.types.validators.ValidatorEtr

/**
 * Lifts Simple Validator to Validator type.
 *
 * @param toBeLifted     Simple validator to be lifted
 * @param none           Value to be returned in case of no failure
 * @param <FailureT>
 * @param <ValidatableT>
 * @return Validator
</ValidatableT></FailureT> */
fun <FailureT, ValidatableT> liftToEtr(
  toBeLifted: Validator<in ValidatableT?, FailureT?>,
  none: FailureT?
): ValidatorEtr<ValidatableT?, FailureT?> = ValidatorEtr {
  it.flatMap { validatable ->
    val result = toBeLifted.unchecked().apply(validatable)
    if (result == none) it else left(result)
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
fun <FailureT, ValidatableT> liftAllToEtr(
  toBeLiftedFns: Collection<Validator<in ValidatableT?, FailureT?>>,
  none: FailureT
): List<ValidatorEtr<ValidatableT?, FailureT?>> =
  toBeLiftedFns.map { liftToEtr(it, none) }
