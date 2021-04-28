package org.revcloud.vader.runner.spec;

import consumer.failure.ValidationFailure;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.Runner;
import org.revcloud.vader.runner.ValidationConfig;

import java.util.List;

import static consumer.failure.ValidationFailure.INVALID_COMBO_1;
import static consumer.failure.ValidationFailure.NONE;
import static org.assertj.core.api.Assertions.assertThat;

class Spec_11_11_n_Test {
    @Test
    void failFastWithInvalidIdForSimpleValidators2() {
        val validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(spec -> List.of(
                spec._11_11_n().orFailWith(INVALID_COMBO_1)
                        .when(Bean::getValueStr).is(null)
                        .then(Bean::getValue)
                        .shouldMatchField(Bean::getDependentValue1)))
                .prepare();
        val invalidBean = new Bean(1, null, 2, 1);
        val failureResult = Runner.validateAndFailFast(invalidBean, NONE, ignore -> NONE, validationConfig);
        assertThat(failureResult).contains(INVALID_COMBO_1);

        val validBean = new Bean(1, null, 1, 2);
        val noneResult = Runner.validateAndFailFast(validBean, NONE, ignore -> NONE, validationConfig);
        assertThat(noneResult).isEmpty();
    }

    @Test
    void failFastWithInvalidIdForSimpleValidators3() {
        val validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(spec -> List.of(
                spec._11_11_n().orFailWith(INVALID_COMBO_1)
                        .when(Bean::getValueStr).is(null)
                        .then(Bean::getValue)
                        .shouldMatchAnyOfFields(List.of(Bean::getDependentValue1, Bean::getDependentValue2))))
                .prepare();
        val invalidBean = new Bean(1, null, 2, 2);
        val failureResult = Runner.validateAndFailFast(invalidBean, NONE, ignore -> NONE, validationConfig);
        assertThat(failureResult).contains(INVALID_COMBO_1);

        val validBean = new Bean(1, null, 2, 1);
        val noneResult = Runner.validateAndFailFast(validBean, NONE, ignore -> NONE, validationConfig);
        assertThat(noneResult).isEmpty();
    }

    @Value
    private static class Bean {
        Integer value;
        String valueStr;
        Integer dependentValue1;
        Integer dependentValue2;
    }
}


