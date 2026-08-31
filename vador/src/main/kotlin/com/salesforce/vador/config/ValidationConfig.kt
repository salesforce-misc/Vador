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
internal abstract class AbstractValidationConfig<ValidatableT, FailureT> :
  AbstractValidationConfigSupport<ValidatableT, FailureT>() {

  abstract class Builder<ValidatableT, FailureT> :
    ConfigBuilder<ValidationConfig<ValidatableT, FailureT>>,
    ValidationBuilderDsl<ValidatableT, FailureT, ValidationConfig.Builder<ValidatableT, FailureT>>
}
