package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate")
public class BatchValidationConfig<ValidatableT, FailureT> extends BaseValidationConfig<ValidatableT, FailureT> {
    Function1<ValidatableT, ?> findDuplicatesWith;
    FailureT andFailDuplicatesWith;
}
