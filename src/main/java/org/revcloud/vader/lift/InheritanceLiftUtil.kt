@file:JvmName("InheritanceLiftUtil")
package org.revcloud.vader.lift

import io.vavr.control.Either
import org.revcloud.vader.types.validators.Validator

fun <ParentT, ValidatableT : ParentT, FailureT> liftToChildValidatorType(
    parentValidator: Validator<ParentT, FailureT>
): Validator<ValidatableT, FailureT> =
    Validator { childValidatable -> parentValidator.apply(Either.narrow(childValidatable)) }

fun <ParentT, ValidatableT : ParentT, FailureT> liftAllToChildValidatorType(
    parentValidators: Collection<Validator<ParentT, FailureT>>
): List<Validator<ValidatableT, FailureT>> = parentValidators.map { liftToChildValidatorType(it) }
