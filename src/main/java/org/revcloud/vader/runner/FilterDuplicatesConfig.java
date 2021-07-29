package org.revcloud.vader.runner;

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
  @Nullable
  Function1<ValidatableT, ?> findAndFilterDuplicatesWith;
  @Nullable FailureT andFailDuplicatesWith;
  @Nullable FailureT andFailNullKeysWith;
}
