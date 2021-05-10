package org.revcloud.vader.runner;

import io.vavr.Function1;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;

@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate")
public class BatchValidationConfig<ValidatableT, FailureT> extends BaseValidationConfig<ValidatableT, FailureT> {
    // These two params are separated out, as `andFailDuplicatesWith` is not mandatory for filter duplicates. You may want to just filter without failing duplicates. 
    @Nullable
    Function1<ValidatableT, ?> findAndFilterDuplicatesWith;
    @Nullable
    FailureT andFailDuplicatesWith;
    @Nullable
    FailureT andFailNullKeysWith;
}
