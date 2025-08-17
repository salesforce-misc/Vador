/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.types

import io.vavr.CheckedFunction1
import io.vavr.control.Either
import java.lang.reflect.Field

fun interface Validator<ValidatableT, FailureT> : CheckedFunction1<ValidatableT, FailureT>

fun interface ValidatorEtr<ValidatableT, FailureT> :
  CheckedFunction1<Either<FailureT?, ValidatableT?>, Either<FailureT?, *>>

fun interface ValidatorAnnotation1<FieldT, FailureT> {
  fun validate(field: Field, value: FieldT?, failure: FailureT, none: FailureT): FailureT
}

fun interface ValidatorAnnotation2<FieldT, FailureT> {
  fun validate(
    field: Field,
    value1: FieldT,
    value2: FieldT,
    failure: FailureT,
    none: FailureT,
  ): FailureT
}
