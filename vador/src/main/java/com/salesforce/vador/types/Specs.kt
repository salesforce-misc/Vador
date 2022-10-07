/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.types

import com.salesforce.vador.specs.factory.SpecFactory
import com.salesforce.vador.specs.specs.base.BaseSpec

fun interface Spec<ValidatableT, FailureT> :
  Function1<SpecFactory<ValidatableT, FailureT>, BaseSpec.BaseSpecBuilder<ValidatableT, FailureT, *, *>>

fun interface Specs<ValidatableT, FailureT> :
  Function1<SpecFactory<ValidatableT, FailureT>, Collection<BaseSpec.BaseSpecBuilder<ValidatableT, FailureT, *, *>>>
