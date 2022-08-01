/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.runner;

import de.cronn.reflection.util.TypedPropertyGetter;
import java.util.Collection;
import java.util.Set;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.runner.config.BaseContainerValidationConfig;
import org.revcloud.vader.runner.config.ContainerValidationConfig;

@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class ContainerValidationConfigWith2Levels<
        ContainerRootLevelValidatableT, ContainerLevel1ValidatableT, FailureT>
    extends BaseContainerValidationConfig<ContainerRootLevelValidatableT, FailureT> {

  @Singular
  Collection<
          TypedPropertyGetter<
              ContainerRootLevelValidatableT, @Nullable Collection<ContainerLevel1ValidatableT>>>
      withBatchMembers;

  ContainerValidationConfig<ContainerLevel1ValidatableT, FailureT> withScopeOf1LevelDeep;

  public Set<String> getFieldNamesForBatchLevel1(
      Class<ContainerLevel1ValidatableT> validatableClazz) {
    return ContainerValidationConfigEx.getFieldNamesForBatchLevel1Ex(this, validatableClazz);
  }

  public Set<String> getFieldNamesForBatchRootLevel(
      Class<ContainerRootLevelValidatableT> validatableClazz) {
    return ContainerValidationConfigEx.getFieldNamesForBatchEx(this, validatableClazz);
  }
}
