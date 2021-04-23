package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate")
public class BatchValidationConfig<ValidatableT, FailureT> extends BaseValidationConfig<ValidatableT, FailureT> {
    // These two params are separated out, as `andFailDuplicatesWith` is not mandatory for filter duplicates. You may want to just filter without failing duplicates. 
    Function1<ValidatableT, ?> findDuplicatesWith;
    FailureT andFailDuplicatesWith;
    boolean isAllOrNone;
    Function1<ValidatableT, Boolean> isAllOrNoneAsPer;
}
