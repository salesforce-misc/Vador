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

package org.revcloud.vader.config;

import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Function2;
import io.vavr.Tuple2;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@Builder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class FieldConfig<FieldT, ValidatableT, FailureT> {
  @Nullable Predicate<FieldT> withFieldValidator;

  @Singular("shouldHaveValidFormatOrFailWith")
  Map<TypedPropertyGetter<ValidatableT, FieldT>, FailureT> shouldHaveValidFormatForAllOrFailWith;

  @Nullable
  Tuple2<
          @NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, @Nullable FieldT>>,
          @NonNull Function2<String, @Nullable FieldT, @Nullable FailureT>>
      shouldHaveValidFormatForAllOrFailWithFn;

  @Singular("shouldHaveValidFormatOrFailWithFn")
  Map<TypedPropertyGetter<ValidatableT, FieldT>, Function2<String, FieldT, FailureT>>
      shouldHaveValidFormatOrFailWithFn;

  @Singular("absentOrHaveValidFormatOrFailWith")
  Map<TypedPropertyGetter<ValidatableT, FieldT>, FailureT> absentOrHaveValidFormatForAllOrFailWith;

  @Nullable
  Tuple2<
          @NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, @Nullable FieldT>>,
          @NonNull Function2<String, @Nullable FieldT, @Nullable FailureT>>
      absentOrHaveValidFormatForAllOrFailWithFn;

  @Singular("absentOrHaveValidFormatOrFailWithFn")
  Map<TypedPropertyGetter<ValidatableT, FieldT>, Function2<String, FieldT, FailureT>>
      absentOrHaveValidFormatOrFailWithFn;
}
