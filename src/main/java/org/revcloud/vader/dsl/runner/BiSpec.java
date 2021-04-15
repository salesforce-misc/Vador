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
public class BiSpec<ValidatableT, FailureT> extends BaseSpec<ValidatableT, FailureT> {
    Function1<ValidatableT, ?> when;
    Object is;
    Function1<ValidatableT, ?> then;

    // TODO 15/04/21 gopala.akshintala: Check for non-null 
    @Override
    public Predicate<ValidatableT> toPredicate() {
        return validatable -> {
            val actualFieldValue = getWhen().apply(validatable);
            if (actualFieldValue != getIs()) {
                return true;
            }
            val actualDependentFieldValue = getThen().apply(validatable);
            final Matcher<?> shouldBe = getShouldBe();
            return shouldBe != null && shouldBe.matches(actualDependentFieldValue) ||
                    matchFields(this, validatable, actualDependentFieldValue);
        };
    }
}
