/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.runner;

import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Function2;
import io.vavr.Tuple2;
import java.util.Collection;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

// ! TODO 16/04/22: Extract common types to type aliases,
//  like `shouldHaveValidSFIdFormatForAllOrFailWith` and
// `absentOrHaveValidSFIdFormatForAllOrFailWith`
@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@Builder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class IDConfig<IDT, ValidatableT, FailureT, EntityIdInfoT> {
  @Nullable Function2<IDT, EntityIdInfoT, @NonNull Boolean> withIdValidator;

  @Singular("shouldHaveValidSFIdFormatOrFailWith")
  Map<
          Tuple2<
              @NonNull TypedPropertyGetter<ValidatableT, @Nullable IDT>, ? extends EntityIdInfoT>,
          @Nullable FailureT>
      shouldHaveValidSFIdFormatForAllOrFailWith;

  @Singular("shouldHaveValidSFPolymorphicIdFormatOrFailWith")
  Map<
          Tuple2<
              @NonNull TypedPropertyGetter<ValidatableT, @Nullable IDT>,
              ? extends Collection<? extends EntityIdInfoT>>,
          @Nullable FailureT>
      shouldHaveValidSFPolymorphicIdFormatForAllOrFailWith;

  @Nullable
  Tuple2<
          @NonNull Map<TypedPropertyGetter<ValidatableT, @Nullable IDT>, ? extends EntityIdInfoT>,
          @NonNull Function2<String, @Nullable IDT, @Nullable FailureT>>
      shouldHaveValidSFIdFormatForAllOrFailWithFn;

  @Nullable
  Tuple2<
          @NonNull Map<
              TypedPropertyGetter<ValidatableT, @Nullable IDT>,
              ? extends Collection<? extends EntityIdInfoT>>,
          @NonNull Function2<String, @Nullable IDT, @Nullable FailureT>>
      shouldHaveValidSFPolymorphicIdFormatForAllOrFailWithFn;

  @Singular("shouldHaveValidSFIdFormatOrFailWithFn")
  Map<
          Tuple2<
              @NonNull TypedPropertyGetter<ValidatableT, @Nullable IDT>, ? extends EntityIdInfoT>,
          @NonNull Function2<String, @Nullable IDT, @Nullable FailureT>>
      shouldHaveValidSFIdFormatOrFailWithFn;

  @Singular("shouldHaveValidSFPolymorphicIdFormatOrFailWithFn")
  Map<
          Tuple2<
              @NonNull TypedPropertyGetter<ValidatableT, @Nullable IDT>,
              ? extends Collection<? extends EntityIdInfoT>>,
          @NonNull Function2<String, @Nullable IDT, @Nullable FailureT>>
      shouldHaveValidSFPolymorphicIdFormatOrFailWithFn;

  @Singular("absentOrHaveValidSFIdFormatOrFailWith")
  Map<
          Tuple2<
              @NonNull TypedPropertyGetter<ValidatableT, @Nullable IDT>, ? extends EntityIdInfoT>,
          @Nullable FailureT>
      absentOrHaveValidSFIdFormatForAllOrFailWith;

  @Singular("absentOrHaveValidSFPolymorphicIdFormatOrFailWith")
  Map<
          Tuple2<
              @NonNull TypedPropertyGetter<ValidatableT, @Nullable IDT>,
              ? extends Collection<? extends EntityIdInfoT>>,
          @Nullable FailureT>
      absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWith;

  @Nullable
  Tuple2<
          @NonNull Map<TypedPropertyGetter<ValidatableT, IDT>, ? extends EntityIdInfoT>,
          @NonNull Function2<String, @Nullable IDT, @Nullable FailureT>>
      absentOrHaveValidSFIdFormatForAllOrFailWithFn;

  @Nullable
  Tuple2<
          @NonNull Map<
              TypedPropertyGetter<ValidatableT, @Nullable IDT>,
              ? extends Collection<? extends EntityIdInfoT>>,
          @NonNull Function2<String, @Nullable IDT, @Nullable FailureT>>
      absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWithFn;

  @Singular("absentOrHaveValidSFIdFormatOrFailWithFn")
  Map<
          Tuple2<
              @NonNull TypedPropertyGetter<ValidatableT, @Nullable IDT>, ? extends EntityIdInfoT>,
          @NonNull Function2<String, @Nullable IDT, @Nullable FailureT>>
      absentOrHaveValidSFIdFormatOrFailWithFn;

  @Singular("absentOrHaveValidSFPolymorphicIdFormatOrFailWithFn")
  Map<
          Tuple2<
              @NonNull TypedPropertyGetter<ValidatableT, @Nullable IDT>,
              ? extends Collection<? extends EntityIdInfoT>>,
          @NonNull Function2<String, @Nullable IDT, @Nullable FailureT>>
      absentOrHaveValidSFPolymorphicIdFormatOrFailWithFn;
}
