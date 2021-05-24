@file:JvmName("InheritanceLiftEtrUtil")

package org.revcloud.vader.lift

import io.vavr.control.Either.narrow
import org.revcloud.vader.types.validators.ValidatorEtr

/**
 * Assume `ParentT` is parent by inheritance to `ValidatableT`.
 * To validate `ValidatableT`, all it's validations + all its Parent validations has to pass.
 * These utils help to lift it's parent's validations in the context of ValidatableT, so that they can be chained together.
 */
fun <ParentT, ValidatableT : ParentT, FailureT> liftToChildValidatorType(
    parentValidatorEtr: ValidatorEtr<ParentT, FailureT>
): ValidatorEtr<ValidatableT, FailureT> =
    ValidatorEtr { childValidatable -> parentValidatorEtr.apply(narrow(childValidatable)) }

fun <ParentT, ValidatableT : ParentT, FailureT> liftAllToChildValidatorType(
    parentValidatorEtrs: Collection<ValidatorEtr<ParentT, FailureT>>
): List<ValidatorEtr<ValidatableT, FailureT>> = parentValidatorEtrs.map { liftToChildValidatorType(it) }
