/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.specs.specs;

import io.vavr.Function1;
import java.util.Collection;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.specs.specs.base.BaseSpec;

@Value
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PACKAGE)
@SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
public class Spec1<ValidatableT, FailureT, GivenT> extends BaseSpec<ValidatableT, FailureT> {

  @NonNull Function1<ValidatableT, ? extends GivenT> given;

  @Singular("shouldMatchField")
  Collection<Function1<ValidatableT, ?>> shouldMatchAnyOfFields;

  @Singular("shouldMatch")
  Collection<? extends Matcher<? extends GivenT>> shouldMatchAnyOf;

  @Nullable Function1<GivenT, ? extends FailureT> orFailWithFn;

  @Override
  public Predicate<@NonNull ValidatableT> toPredicate() {
    return SpecEx.toPredicateEx(this);
  }

  @Override
  public FailureT getFailure(@Nullable ValidatableT validatable) {
    if ((orFailWith == null) == (orFailWithFn == null)) {
      throw new IllegalArgumentException(String.format(INVALID_FAILURE_CONFIG, nameForTest));
    }
    if (orFailWith != null) {
      return orFailWith;
    }
    return orFailWithFn.apply(getGiven().apply(validatable));
  }
}
