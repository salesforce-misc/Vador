package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import io.vavr.Tuple2;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate")
public final class BatchValidationConfig<ValidatableT, FailureT> extends BaseValidationConfig<ValidatableT, FailureT> {
    private final Tuple2<FailureT, Function1<ValidatableT, ?>> shouldFilterDuplicates;
}
