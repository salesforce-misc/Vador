package org.revcloud.vader.runner.spec;

import consumer.failure.ValidationFailure;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.Runner;
import org.revcloud.vader.runner.ValidationConfig;

import java.util.List;

import static consumer.failure.ValidationFailure.INVALID_COMBO_1;
import static consumer.failure.ValidationFailure.INVALID_COMBO_2;
import static consumer.failure.ValidationFailure.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;

class Spec_11_1n_Test {
    // TODO 27/04/21 gopala.akshintala: Replicate these tests for Batch 
    @Test
    void failFastWithInvalidIdForSimpleValidators() {
        val validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(spec -> List.of(
                spec._11_1n().orFailWith(INVALID_COMBO_1)
                        .when(Bean::getValue).is(1)
                        .then(Bean::getValueStr)
                        .shouldBe(either(is("one")).or(is("1"))),
                spec._11_1n().orFailWith(INVALID_COMBO_2)
                        .when(Bean::getValue).is(2)
                        .then(Bean::getValueStr).shouldBe(either(is("two")).or(is("2")))))
                .prepare();

        val invalidBean1 = new Bean(1, "a", null, null);
        val failureResult1 = Runner.validateAndFailFast(invalidBean1, NONE, ignore -> NONE, validationConfig);
        assertThat(failureResult1).contains(INVALID_COMBO_1);

        val invalidBean2 = new Bean(2, "b", null, null);
        val failureResult2 = Runner.validateAndFailFast(invalidBean2, NONE, ignore -> NONE, validationConfig);
        assertThat(failureResult2).contains(INVALID_COMBO_2);

        val validBean1 = new Bean(1, "one", null, null);
        val noneResult1 = Runner.validateAndFailFast(validBean1, NONE, ignore -> NONE, validationConfig);
        assertThat(noneResult1).isEmpty();

        val validBean2 = new Bean(2, "two", null, null);
        val noneResult2 = Runner.validateAndFailFast(validBean2, NONE, ValidationFailure::getValidationFailureForException, validationConfig);
        assertThat(noneResult2).isEmpty();
    }

    @Test
    void spec2WithName() {
        val invalidCombo1 = "invalidCombo1";
        val invalidCombo2 = "invalidCombo2";
        val validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(spec -> List.of(
                spec._11_1n().nameForTest(invalidCombo1)
                        .orFailWith(INVALID_COMBO_1)
                        .when(Bean::getValue).is(1)
                        .then(Bean::getValueStr).shouldBe(either(is("one")).or(is("1"))),
                spec._11_1n().nameForTest(invalidCombo2)
                        .orFailWith(INVALID_COMBO_2)
                        .when(Bean::getValue).is(2)
                        .then(Bean::getValueStr).shouldBe(either(is("two")).or(is("2")))))
                .prepare();
        val invalidBean1 = new Bean(1, "a", null, null);
        Assertions.assertFalse(validationConfig.getSpecWithName(invalidCombo1).map(spec -> spec.test(invalidBean1)).orElse(true));

        val invalidBean2 = new Bean(2, "b", null, null);
        Assertions.assertFalse(validationConfig.getSpecWithName(invalidCombo2).map(spec -> spec.test(invalidBean2)).orElse(true));

        val validBean1 = new Bean(1, "one", null, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidCombo1).map(spec -> spec.test(validBean1)).orElse(false));

        val validBean2 = new Bean(2, "two", null, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidCombo2).map(spec -> spec.test(validBean2)).orElse(false));
    }

    @Value
    private static class Bean {
        Integer value;
        String valueStr;
        Integer dependentValue1;
        Integer dependentValue2;
    }
}


