/**
 * ****************************************************************************
 * Copyright (c) 2026, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config

import com.salesforce.vador.config.base.BaseFieldConfig
import com.salesforce.vador.config.base.BaseFilterDuplicatesConfig
import com.salesforce.vador.config.base.BaseIDConfig

interface ConfigBuilder<out ConfigT> {
  fun prepare(): ConfigT
}

/** Generator-neutral builder protocol for ID validation child configurations. */
sealed interface IDConfigBuilder<IDT, ValidatableT, FailureT, EntityIdInfoT> :
  ConfigBuilder<BaseIDConfig<IDT, ValidatableT, FailureT, EntityIdInfoT>>

/** Generator-neutral builder protocol for field validation child configurations. */
sealed interface FieldConfigBuilder<FieldT, ValidatableT, FailureT> :
  ConfigBuilder<BaseFieldConfig<FieldT, ValidatableT, FailureT>>

/** Generator-neutral builder protocol for duplicate-filter child configurations. */
sealed interface FilterDuplicatesConfigBuilder<ValidatableT, FailureT> :
  ConfigBuilder<BaseFilterDuplicatesConfig<ValidatableT, FailureT>>
