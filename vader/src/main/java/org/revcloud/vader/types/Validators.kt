package org.revcloud.vader.types

import io.vavr.CheckedFunction1
import io.vavr.control.Either

fun interface Validator<ValidatableT, FailureT> : CheckedFunction1<ValidatableT, FailureT>

fun interface ValidatorEtr<ValidatableT, FailureT> :
  CheckedFunction1<Either<FailureT?, ValidatableT?>, Either<FailureT?, *>>
