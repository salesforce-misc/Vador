/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config.container

import com.salesforce.vador.immutables.ConfigStyle
import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorEtr
import de.cronn.reflection.util.TypedPropertyGetter
import org.immutables.value.Value

@ConfigStyle
@Value.Immutable(copy = false)
internal abstract class AbstractContainerValidationConfig<ContainerValidatableT, FailureT> :
  BaseSingleLevelContainerValidationConfig<ContainerValidatableT, FailureT>() {

  @get:Value.Auxiliary
  protected abstract val containerValidatorEtrs: List<ValidatorEtr<ContainerValidatableT, FailureT>>

  @get:Value.Derived
  override val withContainerValidatorEtrs: Collection<ValidatorEtr<ContainerValidatableT, FailureT>>
    get() = containerValidatorEtrs

  @get:Value.Auxiliary
  protected abstract val containerValidatorMappings:
    Map<Validator<in ContainerValidatableT, FailureT>, FailureT>

  @get:Value.Derived
  override val withContainerValidator: Map<Validator<in ContainerValidatableT, FailureT>, FailureT>
    get() = containerValidatorMappings

  @get:Value.Auxiliary
  protected abstract val batchMemberGetters:
    List<TypedPropertyGetter<ContainerValidatableT, Collection<*>?>>

  @get:Value.Derived
  override val withBatchMembers:
    Collection<TypedPropertyGetter<ContainerValidatableT, Collection<*>?>>
    get() = batchMemberGetters

  abstract class Builder<ContainerValidatableT, FailureT> :
    ContainerValidationBuilderDsl<
      ContainerValidatableT,
      FailureT,
      ContainerValidationConfig.Builder<ContainerValidatableT, FailureT>,
    >,
    ContainerBatchMembersBuilderDsl<
      ContainerValidatableT,
      Collection<*>?,
      ContainerValidationConfig.Builder<ContainerValidatableT, FailureT>,
    >
}
