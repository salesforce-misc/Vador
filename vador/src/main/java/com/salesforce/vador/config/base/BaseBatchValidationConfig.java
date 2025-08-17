/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.config.base;

import com.salesforce.vador.config.FilterDuplicatesConfig.FilterDuplicatesConfigBuilder;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;

@Getter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public abstract class BaseBatchValidationConfig<ValidatableT, FailureT>
		extends BaseValidationConfig<ValidatableT, FailureT> {

	@Singular
	protected Collection<FilterDuplicatesConfigBuilder<ValidatableT, @Nullable FailureT>>
			findAndFilterDuplicatesConfigs;
}
