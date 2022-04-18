package org.revcloud.vader.runner;

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
  Map<@NonNull TypedPropertyGetter<ValidatableT, @Nullable FieldT>, @Nullable FailureT>
      shouldHaveValidFormatForAllOrFailWith;

  @Nullable
  Tuple2<
          @NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, @Nullable FieldT>>,
          @NonNull Function2<String, @Nullable FieldT, @Nullable FailureT>>
      shouldHaveValidFormatForAllOrFailWithFn;

  @Singular("shouldHaveValidFormatOrFailWithFn")
  Map<
          @NonNull TypedPropertyGetter<ValidatableT, @Nullable FieldT>,
          @NonNull Function2<String, @Nullable FieldT, @Nullable FailureT>>
      shouldHaveValidFormatOrFailWithFn;

  @Singular("absentOrHaveValidFormatOrFailWith")
  Map<@NonNull TypedPropertyGetter<ValidatableT, @Nullable FieldT>, @Nullable FailureT>
      absentOrHaveValidFormatForAllOrFailWith;

  @Nullable
  Tuple2<
          @NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, @Nullable FieldT>>,
          @NonNull Function2<String, @Nullable FieldT, @Nullable FailureT>>
      absentOrHaveValidFormatForAllOrFailWithFn;

  @Singular("absentOrHaveValidFormatOrFailWithFn")
  Map<
          @NonNull TypedPropertyGetter<ValidatableT, @Nullable FieldT>,
          @NonNull Function2<String, @Nullable FieldT, @Nullable FailureT>>
      absentOrHaveValidFormatOrFailWithFn;
}
