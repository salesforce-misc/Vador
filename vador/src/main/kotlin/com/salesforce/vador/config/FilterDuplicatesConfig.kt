/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config

import com.salesforce.vador.immutables.ConfigStyle
import io.vavr.Function1
import org.immutables.value.Value
import org.jetbrains.annotations.Nullable

@ConfigStyle
@Value.Immutable(copy = false)
internal abstract class AbstractFilterDuplicatesConfig<ValidatableT, FailureT> {
  // `andFailDuplicatesWith` is not mandatory for `findAndFilterDuplicatesWith`.
  // You may want to just filter without failing duplicates. So they are separated
  @get:Nullable abstract val findAndFilterDuplicatesWith: Function1<ValidatableT, *>?

  @get:Nullable abstract val andFailDuplicatesWith: FailureT?

  @get:Nullable abstract val andFailNullKeysWith: FailureT?
}
