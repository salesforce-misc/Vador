package org.revcloud.vader.dsl.runner;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public final class ValidationConfig<ValidatableT, FailureT> extends BaseValidationConfig<ValidatableT, FailureT> {
}
