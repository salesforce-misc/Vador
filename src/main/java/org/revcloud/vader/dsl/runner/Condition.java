package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import lombok.Builder;
import lombok.Getter;
import org.hamcrest.Matcher;


@Builder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
@Getter
public class Condition<ValidatableT, FailureT> {
    private final FailureT orFailWith;
    private final Function1<ValidatableT, ?> when;
    private final Object is;
    private final Function1<ValidatableT, ?> then;
    private final Matcher<?> shouldBe;
}
