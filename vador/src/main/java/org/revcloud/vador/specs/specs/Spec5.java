/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vador.specs.specs;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple2;
import java.util.Collection;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vador.specs.specs.base.BaseSpec;

@Value
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PACKAGE)
@SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
public class Spec5<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
  @NonNull
  Tuple2<@Nullable Collection<Function1<ValidatableT, ?>>, @Nullable Matcher<?>>
      whenAllTheseFieldsMatch;

  @NonNull
  Tuple2<@Nullable Collection<Function1<ValidatableT, ?>>, @Nullable Matcher<?>>
      thenAllThoseFieldsShouldMatch;

  @Nullable Function2<Collection<?>, Collection<?>, ? extends FailureT> orFailWithFn;

  @Override
  public Predicate<@Nullable ValidatableT> toPredicate() {
    return SpecEx.toPredicateEx(this);
  }
}
