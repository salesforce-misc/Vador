package org.revcloud.vader.dsl.runner;

import consumer.failure.ValidationFailure;
import io.vavr.Function1;
import io.vavr.collection.List;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static consumer.failure.ValidationFailure.INVALID_COMBO;
import static consumer.failure.ValidationFailure.NONE;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;

class RunnerDslWithConditionTest {
    @Test
    void failFastWithInvalidIdForSimpleValidators() {
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate().withConditions(List.of(
                        Condition.<Bean, ValidationFailure>check().orFailWith(INVALID_COMBO)
                                .when(Bean::getType).is("number").then(Bean::getValue)
                                .then(Bean::getValue).shouldBe(either(is("one")).or(is("two")))
                                .done())).prepare();
        val invalidBean = new Bean("number", "a");
        val failureResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, invalidBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(INVALID_COMBO, failureResult);

        val validBean = new Bean("number", "one");
        val noneResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, validBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(NONE, noneResult);
    }

    private static <ValidatableT, FailureT> FailureT validateAndFailFastForSimpleValidatorsWithConfig(
            FailureT none,
            FailureT nothingToValidate,
            ValidatableT validatable,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return RunnerDsl.validateAndFailFastForSimpleValidators(
                validatable,
                List.empty(),
                nothingToValidate,
                none,
                throwableMapper,
                validationConfig);
    }
}

@Value
class Bean {
    String type;
    String value;
}
