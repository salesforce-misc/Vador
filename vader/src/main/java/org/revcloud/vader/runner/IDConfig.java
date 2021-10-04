package org.revcloud.vader.runner;

import com.force.swag.id.ID;
import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Function2;
import io.vavr.Tuple2;
import java.util.Map;
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
public class IDConfig<ValidatableT, FailureT, EntityIdInfoT> {
  @Nullable Function2<ID, EntityIdInfoT, @NonNull Boolean> withIdValidator;

  @Singular("shouldHaveValidSFIdFormatOrFailWith")
  Map<
          Tuple2<@NonNull TypedPropertyGetter<ValidatableT, @Nullable ID>, ? extends EntityIdInfoT>,
          @Nullable FailureT>
      shouldHaveValidSFIdFormatForAllOrFailWith;

  @Nullable
  Tuple2<
          @NonNull Map<TypedPropertyGetter<ValidatableT, @Nullable ID>, ? extends EntityIdInfoT>,
          @NonNull Function2<String, @Nullable ID, @Nullable FailureT>>
      shouldHaveValidSFIdFormatForAllOrFailWithFn;

  @Singular("shouldHaveValidSFIdFormatOrFailWithFn")
  Map<
          Tuple2<@NonNull TypedPropertyGetter<ValidatableT, @Nullable ID>, ? extends EntityIdInfoT>,
          @NonNull Function2<String, @Nullable ID, @Nullable FailureT>>
      shouldHaveValidSFIdFormatOrFailWithFn;

  @Singular("absentOrHaveValidSFIdFormatOrFailWith")
  Map<
          Tuple2<@NonNull TypedPropertyGetter<ValidatableT, @Nullable ID>, ? extends EntityIdInfoT>,
          @Nullable FailureT>
      absentOrHaveValidSFIdFormatForAllOrFailWith;

  @Nullable
  Tuple2<
          @NonNull Map<TypedPropertyGetter<ValidatableT, ID>, ? extends EntityIdInfoT>,
          @NonNull Function2<String, @Nullable ID, @Nullable FailureT>>
      absentOrHaveValidSFIdFormatForAllOrFailWithFn;

  @Singular("absentOrHaveValidSFIdFormatOrFailWithFn")
  Map<
          Tuple2<@NonNull TypedPropertyGetter<ValidatableT, @Nullable ID>, ? extends EntityIdInfoT>,
          @NonNull Function2<String, @Nullable ID, @Nullable FailureT>>
      absentOrHaveValidSFIdFormatOrFailWithFn;
}
