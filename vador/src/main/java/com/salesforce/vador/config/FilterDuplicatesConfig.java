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

package com.salesforce.vador.config;

import io.vavr.Function1;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@Builder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class FilterDuplicatesConfig<ValidatableT, FailureT> {
  // `andFailDuplicatesWith` is not mandatory for `findAndFilterDuplicatesWith`.
  // You may want to just filter without failing duplicates. So they are separated
  @Nullable Function1<ValidatableT, ?> findAndFilterDuplicatesWith;
  @Nullable FailureT andFailDuplicatesWith;
  @Nullable FailureT andFailNullKeysWith;
}
