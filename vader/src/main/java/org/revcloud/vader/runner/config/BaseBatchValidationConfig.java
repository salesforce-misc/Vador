/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.runner.config;

import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.runner.config.FilterDuplicatesConfig.FilterDuplicatesConfigBuilder;

@Getter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public abstract class BaseBatchValidationConfig<ValidatableT, FailureT>
    extends BaseValidationConfig<ValidatableT, FailureT> {

  @Singular
  protected Collection<FilterDuplicatesConfigBuilder<ValidatableT, @Nullable FailureT>>
      findAndFilterDuplicatesConfigs;
}
