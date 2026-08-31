/**
 * ****************************************************************************
 * Copyright (c) 2026, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config.base

import io.vavr.Function1
import org.jetbrains.annotations.Nullable

abstract class BaseFilterDuplicatesConfig<ValidatableT, FailureT> {
  @get:Nullable abstract val findAndFilterDuplicatesWith: Function1<ValidatableT, *>?

  @get:Nullable abstract val andFailDuplicatesWith: FailureT?

  @get:Nullable abstract val andFailNullKeysWith: FailureT?
}
