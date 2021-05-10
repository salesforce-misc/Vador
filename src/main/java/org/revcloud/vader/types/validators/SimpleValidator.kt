package org.revcloud.vader.types.validators

import io.vavr.CheckedFunction1

fun interface SimpleValidator<ValidatableT, FailureT> : CheckedFunction1<ValidatableT, FailureT>
