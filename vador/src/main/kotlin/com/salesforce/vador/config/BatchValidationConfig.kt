/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config

import com.salesforce.vador.immutables.ConfigStyle
import org.immutables.value.Value

@ConfigStyle
@Value.Immutable(copy = false)
internal abstract class AbstractBatchValidationConfig<ValidatableT, FailureT> :
  AbstractBatchValidationConfigSupport<ValidatableT, FailureT>() {

  abstract class Builder<ValidatableT, FailureT> :
    BatchConfigBuilder<ValidatableT, FailureT>,
    ValidationBuilderDsl<
      ValidatableT,
      FailureT,
      BatchValidationConfig.Builder<ValidatableT, FailureT>,
    >,
    BatchValidationBuilderDsl<
      ValidatableT,
      FailureT,
      BatchValidationConfig.Builder<ValidatableT, FailureT>,
    >
}
