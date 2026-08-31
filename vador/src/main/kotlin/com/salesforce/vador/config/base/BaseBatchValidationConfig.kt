/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config.base

import com.salesforce.vador.config.FilterDuplicatesConfigBuilder

abstract class BaseBatchValidationConfig<ValidatableT, FailureT> :
  BaseValidationConfig<ValidatableT, FailureT>() {

  abstract val findAndFilterDuplicatesConfigs:
    Collection<FilterDuplicatesConfigBuilder<ValidatableT, FailureT?>>
}
