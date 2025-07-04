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

package com.salesforce.vador.config.container;

import com.salesforce.vador.config.base.BaseContainerValidationConfig;
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

@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class ContainerValidationConfig<ContainerValidatableT, FailureT>
		extends BaseContainerValidationConfig<ContainerValidatableT, FailureT> {

	@Singular
	Collection<TypedPropertyGetter<ContainerValidatableT, @Nullable Collection<?>>> withBatchMembers;

	public Set<String> getFieldNamesForBatch(Class<ContainerValidatableT> validatableClazz) {
		return ContainerValidationConfigEx.getFieldNamesForBatchEx(this, validatableClazz);
	}
}
