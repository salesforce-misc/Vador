package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.hamcrest.Matcher;

import java.util.function.Predicate;

import static org.revcloud.vader.dsl.runner.Utils.matchFields;

@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "done", builderMethodName = "check", toBuilder = true)
public class Spec<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
    Function1<ValidatableT, ?> given;

    @Override
    public Predicate<ValidatableT> toPredicate() {
        return validatable -> {
            val actualFieldValue = getGiven().apply(validatable);
            final Matcher<?> shouldBe = getShouldBe();
            return shouldBe != null && shouldBe.matches(actualFieldValue) ||
                    matchFields(this, validatable, actualFieldValue);
        };
    }
}
