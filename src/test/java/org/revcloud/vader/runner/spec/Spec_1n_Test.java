package org.revcloud.vader.runner.spec;

import consumer.failure.ValidationFailure;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.Runner;
import org.revcloud.vader.runner.ValidationConfig;

import static consumer.failure.ValidationFailure.INVALID_VALUE;
import static consumer.failure.ValidationFailure.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;

class Spec_1n_Test {
    @Test
    void failFastWithInvalidIdForSimpleValidators() {
        val validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSpec(spec ->
                spec._1n().orFailWith(INVALID_VALUE)
                        .given(Bean::getValue)
                        .shouldBe(either(is(1)).or(is(2))))
                .prepare();
        val invalidBean = new Bean(3);
        val failureResult = Runner.validateAndFailFast(invalidBean, NONE, ignore -> NONE, validationConfig);
        assertThat(failureResult).contains(INVALID_VALUE);

        val validBean = new Bean(1);
        val noneResult = Runner.validateAndFailFast(validBean, NONE, ignore -> NONE, validationConfig);
        assertThat(noneResult).isEmpty();
    }
    
    @Value
    static class Bean {
        Integer value;
    }
}


