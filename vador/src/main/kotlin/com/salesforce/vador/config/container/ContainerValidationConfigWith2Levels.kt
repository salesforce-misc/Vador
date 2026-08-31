/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config.container

import com.salesforce.vador.config.base.BaseContainerValidationConfig
import com.salesforce.vador.immutables.ConfigStyle
import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorEtr
import de.cronn.reflection.util.TypedPropertyGetter
import org.immutables.value.Value

internal abstract class AbstractContainerValidationConfigWith2LevelsSupport<
  ContainerRootLevelValidatableT,
  ContainerLevel1ValidatableT,
  FailureT,
> : BaseContainerValidationConfig<ContainerRootLevelValidatableT, FailureT>() {

  abstract val withBatchMembers:
    Collection<
      TypedPropertyGetter<ContainerRootLevelValidatableT, Collection<ContainerLevel1ValidatableT>?>
    >
}

@ConfigStyle
@Value.Immutable(copy = false)
internal abstract class AbstractContainerValidationConfigWith2Levels<
  ContainerRootLevelValidatableT,
  ContainerLevel1ValidatableT,
  FailureT,
> :
  AbstractContainerValidationConfigWith2LevelsSupport<
    ContainerRootLevelValidatableT,
    ContainerLevel1ValidatableT,
    FailureT,
  >() {

  @get:Value.Auxiliary
  protected abstract val containerValidatorEtrs:
    List<ValidatorEtr<ContainerRootLevelValidatableT, FailureT>>

  @get:Value.Derived
  override val withContainerValidatorEtrs:
    Collection<ValidatorEtr<ContainerRootLevelValidatableT, FailureT>>
    get() = containerValidatorEtrs

  @get:Value.Auxiliary
  protected abstract val containerValidatorMappings:
    Map<Validator<in ContainerRootLevelValidatableT, FailureT>, FailureT>

  @get:Value.Derived
  override val withContainerValidator:
    Map<Validator<in ContainerRootLevelValidatableT, FailureT>, FailureT>
    get() = containerValidatorMappings

  @get:Value.Auxiliary
  protected abstract val batchMemberGetters:
    List<
      TypedPropertyGetter<ContainerRootLevelValidatableT, Collection<ContainerLevel1ValidatableT>?>
    >

  @get:Value.Derived
  override val withBatchMembers:
    Collection<
      TypedPropertyGetter<ContainerRootLevelValidatableT, Collection<ContainerLevel1ValidatableT>?>
    >
    get() = batchMemberGetters

  abstract val withScopeOf1LevelDeep:
    BaseSingleLevelContainerValidationConfig<ContainerLevel1ValidatableT, FailureT>

  @Value.NonAttribute
  fun getFieldNamesForBatchLevel1(
    validatableClazz: Class<ContainerLevel1ValidatableT>
  ): Set<String> = getFieldNamesForBatchLevel1Ex(validatableClazz)

  @Value.NonAttribute
  fun getFieldNamesForBatchRootLevel(
    validatableClazz: Class<ContainerRootLevelValidatableT>
  ): Set<String> = getFieldNamesForBatchEx(validatableClazz)

  abstract class Builder<ContainerRootLevelValidatableT, ContainerLevel1ValidatableT, FailureT> :
    ContainerValidationBuilderDsl<
      ContainerRootLevelValidatableT,
      FailureT,
      ContainerValidationConfigWith2Levels.Builder<
        ContainerRootLevelValidatableT,
        ContainerLevel1ValidatableT,
        FailureT,
      >,
    >,
    TwoLevelContainerBatchMembersBuilderDsl<
      ContainerRootLevelValidatableT,
      ContainerLevel1ValidatableT,
      ContainerValidationConfigWith2Levels.Builder<
        ContainerRootLevelValidatableT,
        ContainerLevel1ValidatableT,
        FailureT,
      >,
    >
}
