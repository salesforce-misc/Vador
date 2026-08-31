/**
 * ****************************************************************************
 * Copyright (c) 2026, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config.container

import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorEtr
import de.cronn.reflection.util.TypedPropertyGetter

internal interface ContainerValidationBuilderDsl<ContainerValidatableT, FailureT, SELF> {
  fun containerValidatorEtr(value: ValidatorEtr<ContainerValidatableT, FailureT>): SELF

  fun addAllContainerValidatorEtrs(
    values: Iterable<ValidatorEtr<ContainerValidatableT, FailureT>>
  ): SELF

  fun withContainerValidatorEtr(value: ValidatorEtr<ContainerValidatableT, FailureT>): SELF =
    containerValidatorEtr(value)

  fun withContainerValidatorEtrs(
    values: Iterable<ValidatorEtr<ContainerValidatableT, FailureT>>
  ): SELF = addAllContainerValidatorEtrs(values)

  fun containerValidatorMapping(
    key: Validator<in ContainerValidatableT, FailureT>,
    value: FailureT,
  ): SELF

  fun putAllContainerValidatorMappings(
    entries: Map<out Validator<in ContainerValidatableT, FailureT>, FailureT>
  ): SELF

  fun withContainerValidator(
    key: Validator<in ContainerValidatableT, FailureT>,
    value: FailureT,
  ): SELF = containerValidatorMapping(key, value)

  fun withContainerValidator(
    entries: Map<out Validator<in ContainerValidatableT, FailureT>, FailureT>
  ): SELF = putAllContainerValidatorMappings(entries)
}

internal interface ContainerBatchMembersBuilderDsl<ContainerValidatableT, BatchT, SELF> {
  fun batchMemberGetter(value: TypedPropertyGetter<ContainerValidatableT, BatchT>): SELF

  fun addAllBatchMemberGetters(
    values: Iterable<TypedPropertyGetter<ContainerValidatableT, BatchT>>
  ): SELF

  fun withBatchMember(value: TypedPropertyGetter<ContainerValidatableT, BatchT>): SELF =
    batchMemberGetter(value)

  fun withBatchMembers(values: Iterable<TypedPropertyGetter<ContainerValidatableT, BatchT>>): SELF =
    addAllBatchMemberGetters(values)
}

internal interface TwoLevelContainerBatchMembersBuilderDsl<
  ContainerRootLevelValidatableT,
  ContainerLevel1ValidatableT,
  SELF,
> {
  fun batchMemberGetter(
    value:
      TypedPropertyGetter<
        ContainerRootLevelValidatableT,
        Collection<@JvmSuppressWildcards ContainerLevel1ValidatableT>?,
      >
  ): SELF

  fun addAllBatchMemberGetters(
    values:
      Iterable<
        TypedPropertyGetter<
          ContainerRootLevelValidatableT,
          Collection<@JvmSuppressWildcards ContainerLevel1ValidatableT>?,
        >
      >
  ): SELF

  fun withBatchMember(
    value:
      TypedPropertyGetter<
        ContainerRootLevelValidatableT,
        Collection<@JvmSuppressWildcards ContainerLevel1ValidatableT>?,
      >
  ): SELF = batchMemberGetter(value)

  fun withBatchMembers(
    values:
      Iterable<
        TypedPropertyGetter<
          ContainerRootLevelValidatableT,
          Collection<@JvmSuppressWildcards ContainerLevel1ValidatableT>?,
        >
      >
  ): SELF = addAllBatchMemberGetters(values)
}
