package org.revcloud.vader.runner.spec;

import consumer.failure.ValidationFailure;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.ValidationConfig;

import static consumer.failure.ValidationFailure.INVALID_VALUE;
import static consumer.failure.ValidationFailure.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;
import static org.revcloud.vader.runner.Runner.validateAndFailFast;

class Spec1Test {
    @Test
    void spec1Test() {
        final var validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSpec(spec ->
                spec.<Integer>_1().orFailWith(INVALID_VALUE)
                        .given(Bean::getValue)
                        .shouldMatch(either(is(1)).or(is(2))))
                .prepare();
        final var invalidBean = new Bean(3);
        final var failureResult = validateAndFailFast(invalidBean, ValidationFailure::getValidationFailureForException, validationConfig);
        assertThat(failureResult).contains(INVALID_VALUE);

        final var validBean = new Bean(1);
        final var noneResult = validateAndFailFast(validBean, ignore -> NONE, validationConfig);
        assertThat(noneResult).isEmpty();
    }
    
    @Value
    static class Bean {
        Integer value;
    }
}


