package org.revcloud.vader.runner;

import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.runner.FilterDuplicatesConfig.FilterDuplicatesConfigBuilder;

@Getter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
abstract class BaseBatchValidationConfig<ValidatableT, FailureT>
    extends BaseValidationConfig<ValidatableT, FailureT> {

  @Singular
  protected Collection<FilterDuplicatesConfigBuilder<ValidatableT, @Nullable FailureT>>
      findAndFilterDuplicatesConfigs;
}
