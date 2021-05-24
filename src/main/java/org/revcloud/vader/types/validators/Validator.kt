package org.revcloud.vader.types.validators

import io.vavr.CheckedFunction1

fun interface Validator<ValidatableT, FailureT> : CheckedFunction1<ValidatableT, FailureT>
