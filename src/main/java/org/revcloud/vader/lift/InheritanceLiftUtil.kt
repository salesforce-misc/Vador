@file:JvmName("InheritanceLiftUtil")

package org.revcloud.vader.lift

import io.vavr.control.Either.narrow
import org.revcloud.vader.types.validators.Validator

/**
 * Assume `ParentT` is parent by inheritance to `ValidatableT`.
 * To validate `ValidatableT`, all it's validations + all its Parent validations has to pass.
 * These utils help to lift it's parent's validations in the context of ValidatableT, so that they can be chained together.
 */
fun <ParentT, ValidatableT : ParentT, FailureT> liftToChildValidatorType(
    parentValidator: Validator<ParentT, FailureT>
): Validator<ValidatableT, FailureT> =
    Validator { childValidatable -> parentValidator.apply(narrow(childValidatable)) }

fun <ParentT, ValidatableT : ParentT, FailureT> liftAllToChildValidatorType(
    parentValidators: Collection<Validator<ParentT, FailureT>>
): List<Validator<ValidatableT, FailureT>> = parentValidators.map { liftToChildValidatorType(it) }
