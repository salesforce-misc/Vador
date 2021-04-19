package org.revcloud.vader.dsl.runner.spec;

import consumer.failure.ValidationFailure;
import io.vavr.Function1;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.dsl.runner.RunnerDsl;
import org.revcloud.vader.dsl.runner.ValidationConfig;

import static consumer.failure.ValidationFailure.INVALID_VALUE;
import static consumer.failure.ValidationFailure.NONE;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;

class RunnerDslWithSpecTest {
    @Test
    void failFastWithInvalidIdForSimpleValidators() {
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate().withSpec(spec ->
                        spec._1.orFailWith(INVALID_VALUE)
                                .given(Bean::getValue)
                                .shouldBe(either(is(1)).or(is(2))).done())
                        .prepare();
        val invalidBean = new Bean(3);
        val failureResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, invalidBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(INVALID_VALUE, failureResult);

        val validBean = new Bean(1);
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
                io.vavr.collection.List.empty(), // TODO 12/04/21 gopala.akshintala: migrate to use java immutable collection 
                nothingToValidate,
                none,
                throwableMapper,
                validationConfig);
    }

    @Value
    static class Bean {
        Integer value;
    }
}

