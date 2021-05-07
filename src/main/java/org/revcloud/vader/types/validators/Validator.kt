package org.revcloud.vader.types.validators

import io.vavr.CheckedFunction1
import io.vavr.control.Either

fun interface Validator<ValidatableT, FailureT> :
    CheckedFunction1<Either<FailureT, ValidatableT>, Either<FailureT, out Any?>> {}
