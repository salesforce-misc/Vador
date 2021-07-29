package org.revcloud.vader.runner;

import io.vavr.Function1;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;

@Getter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
abstract class BaseBatchValidationConfig<ValidatableT, FailureT>
    extends BaseValidationConfig<ValidatableT, FailureT> {
  // These two params are separated out, as `andFailDuplicatesWith` is not mandatory for filter
  // duplicates. You may want to just filter without failing duplicates.
  @Nullable protected Function1<ValidatableT, ?> findAndFilterDuplicatesWith;
  @Nullable protected FailureT andFailDuplicatesWith;
  @Nullable protected FailureT andFailNullKeysWith;
  
  @Singular
  protected Collection<FilterDuplicatesConfig<ValidatableT, FailureT>> findAndFilterDuplicatesConfigs;
}
