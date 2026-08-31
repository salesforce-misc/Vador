/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.specs.factory

import com.salesforce.vador.specs.specs.Spec1
import com.salesforce.vador.specs.specs.Spec2
import com.salesforce.vador.specs.specs.Spec3
import com.salesforce.vador.specs.specs.Spec4
import com.salesforce.vador.specs.specs.Spec5

class SpecFactory<ValidatableT, FailureT> {

  @Suppress("FunctionName")
  fun <GivenT> _1(): Spec1.Builder<ValidatableT, FailureT, GivenT> = Spec1.check()

  @Suppress("FunctionName")
  fun <WhenT, ThenT> _2(): Spec2.Builder<ValidatableT, FailureT, WhenT, ThenT> = Spec2.check()

  @Suppress("FunctionName")
  fun <WhenT, Then1T, Then2T> _3(): Spec3.Builder<ValidatableT, FailureT, WhenT, Then1T, Then2T> =
    Spec3.check()

  @Suppress("FunctionName") fun _4(): Spec4.Builder<ValidatableT, FailureT> = Spec4.check()

  @Suppress("FunctionName") fun _5(): Spec5.Builder<ValidatableT, FailureT> = Spec5.check()
}
