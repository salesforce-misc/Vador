/**
 * ****************************************************************************
 * Copyright (c) 2026, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config.container

import com.salesforce.vador.config.base.BaseContainerValidationConfig
import de.cronn.reflection.util.TypedPropertyGetter
import org.immutables.value.Value

abstract class BaseSingleLevelContainerValidationConfig<ContainerValidatableT, FailureT> :
  BaseContainerValidationConfig<ContainerValidatableT, FailureT>() {

  abstract val withBatchMembers:
    Collection<TypedPropertyGetter<ContainerValidatableT, Collection<*>?>>

  @Value.NonAttribute
  fun getFieldNamesForBatch(validatableClazz: Class<ContainerValidatableT>): Set<String> =
    getFieldNamesForBatchEx(validatableClazz)
}
