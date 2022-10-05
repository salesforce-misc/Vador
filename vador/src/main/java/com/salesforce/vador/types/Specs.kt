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
  Function1<_root_ide_package_.com.salesforce.vador.specs.factory.SpecFactory<ValidatableT, FailureT>, _root_ide_package_.com.salesforce.vador.specs.specs.base.BaseSpec.BaseSpecBuilder<ValidatableT, FailureT, *, *>>

fun interface Specs<ValidatableT, FailureT> :
  Function1<_root_ide_package_.com.salesforce.vador.specs.factory.SpecFactory<ValidatableT, FailureT>, Collection<_root_ide_package_.com.salesforce.vador.specs.specs.base.BaseSpec.BaseSpecBuilder<ValidatableT, FailureT, *, *>>>
