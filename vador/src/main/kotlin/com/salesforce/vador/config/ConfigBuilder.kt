/**
 * ****************************************************************************
 * Copyright (c) 2026, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config

interface ConfigBuilder<out ConfigT> {
  fun prepare(): ConfigT
}

/** Generator-neutral builder protocol for ID validation child configurations. */
sealed interface IDConfigBuilder<ValidatableT, FailureT> : ConfigBuilder<Any>

/** Generator-neutral builder protocol for field validation child configurations. */
sealed interface FieldConfigBuilder<ValidatableT, FailureT> : ConfigBuilder<Any>

/** Generator-neutral builder protocol for duplicate-filter child configurations. */
sealed interface FilterDuplicatesConfigBuilder<ValidatableT, FailureT> : ConfigBuilder<Any>

/** Generator-neutral builder protocol for a nested batch validation configuration. */
sealed interface BatchConfigBuilder<ValidatableT, FailureT> : ConfigBuilder<Any>
