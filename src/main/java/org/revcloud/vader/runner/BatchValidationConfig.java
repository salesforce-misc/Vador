package org.revcloud.vader.runner;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class BatchValidationConfig<ValidatableT, FailureT>
    extends BaseBatchValidationConfig<ValidatableT, FailureT> {}
