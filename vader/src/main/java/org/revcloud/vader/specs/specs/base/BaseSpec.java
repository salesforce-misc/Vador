/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.specs.specs.base;

import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;

@Getter
@FieldDefaults(level = AccessLevel.PACKAGE)
@SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
public abstract class BaseSpec<ValidatableT, FailureT> {

  public static final String INVALID_FAILURE_CONFIG =
      "For Spec with: %s Either 'orFailWith' or 'orFailWithFn' should be passed, but not both";
  @Nullable protected String nameForTest;
  @Nullable protected FailureT orFailWith;

  public abstract Predicate<@Nullable ValidatableT> toPredicate();

  // TODO 05/06/21 gopala.akshintala: Replace with `when` expression checking instanceOf
  @SuppressWarnings("unused")
  public FailureT getFailure(@Nullable ValidatableT ignore) {
    return orFailWith;
  }
}
