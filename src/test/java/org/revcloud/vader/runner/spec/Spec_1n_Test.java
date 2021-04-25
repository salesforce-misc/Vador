package org.revcloud.vader.runner.spec;

import consumer.failure.ValidationFailure;
import io.vavr.Function1;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.Runner;
import org.revcloud.vader.runner.ValidationConfig;

import java.util.Optional;

import static consumer.failure.ValidationFailure.INVALID_VALUE;
import static consumer.failure.ValidationFailure.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;

class Spec_1n_Test {
    
    private static <ValidatableT, FailureT> Optional<FailureT> validateAndFailFastForSimpleValidatorsWithConfig(
            FailureT none,
            FailureT nothingToValidate,
            ValidatableT validatable,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return Runner.validateAndFailFast(
                validatable,
                nothingToValidate,
                throwableMapper,
                validationConfig);
    }
    
    @Test
    void failFastWithInvalidIdForSimpleValidators() {
        val validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSpec(spec ->
                spec._1n().orFailWith(INVALID_VALUE)
                        .given(Bean::getValue)
                        .shouldBe(either(is(1)).or(is(2))))
                .prepare();
        val invalidBean = new Bean(3);
        val failureResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, invalidBean, ignore -> NONE, validationConfig);
        assertThat(failureResult).contains(INVALID_VALUE);

        val validBean = new Bean(1);
        val noneResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, validBean, ignore -> NONE, validationConfig);
        assertThat(noneResult).isEmpty();
    }
    
    @Value
    static class Bean {
        Integer value;
    }
}


