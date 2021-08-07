package org.revcloud.vader.runner;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class ValidationConfig<ValidatableT, FailureT>
    extends BaseValidationConfig<ValidatableT, FailureT> {}
