package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
public class Spec<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
    Function1<ValidatableT, ?> given;
}
