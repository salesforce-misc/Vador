package org.revcloud.vader.runner.spec;

import consumer.failure.ValidationFailure;
import io.vavr.collection.HashSet;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.Runner;
import org.revcloud.vader.runner.ValidationConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static consumer.failure.ValidationFailure.INVALID_COMBO_1;
import static consumer.failure.ValidationFailure.INVALID_COMBO_2;
import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.getFailureWithParams;
import static consumer.failure.ValidationFailureMessage.MSG_WITH_PARAMS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class Spec2Test {
    @Test
    void spec2Test() {
        val invalidComboSpec = "invalidComboSpec";
        val validComboMap = Map.of(
                1, Set.of("1", "one"),
                2, Set.of("2", "two")
        );
        val validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSpec(spec ->
                spec._2().nameForTest(invalidComboSpec)
                        .orFailWith(INVALID_COMBO_1)
                        .when(Bean::getValue)
                        .then(Bean::getValueStr)
                        .shouldRelateWith(validComboMap)
                        .orFailWithFn((value, valueStr) -> getFailureWithParams(MSG_WITH_PARAMS, value, valueStr)))
                .prepare();

        val invalidBean1 = new Bean(1, "a", null, null);
        assertFalse(validationConfig.getSpecWithName(invalidComboSpec).map(spec -> spec.test(invalidBean1)).orElse(true));

        val invalidBean2 = new Bean(2, "b", null, null);
        assertFalse(validationConfig.getSpecWithName(invalidComboSpec).map(spec -> spec.test(invalidBean2)).orElse(true));

        val validBean1 = new Bean(1, "one", null, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidComboSpec).map(spec -> spec.test(validBean1)).orElse(false));

        val validBean2 = new Bean(2, "two", null, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidComboSpec).map(spec -> spec.test(validBean2)).orElse(false));
    }

    @Test
    void spec2TestWithNullValue() {
        val invalidComboSpec = "invalidComboSpec";
        val validComboMap = Map.of(
                BillingTerm.OneTime, HashSet.of(null, "1", "one").toJavaSet(),
                BillingTerm.Month, Set.of("2", "two")
        );
        val validationConfig = ValidationConfig.<Bean2, ValidationFailure>toValidate().withSpec(spec ->
                spec._2().nameForTest(invalidComboSpec)
                        .when(Bean2::getBt)
                        .then(Bean2::getValueStr)
                        .shouldRelateWith(validComboMap)
                        .orFailWithFn((value, valueStr) -> getFailureWithParams(MSG_WITH_PARAMS, value, valueStr)))
                .prepare();
        val validBean = new Bean2(BillingTerm.OneTime, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidComboSpec).map(spec -> spec.test(validBean)).orElse(false));
    }

    @Test
    void multiSpec2Test() {
        val validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(spec -> List.of(
                spec.<Integer, String>_2().when(Bean::getValue)
                        .matches(is(1))
                        .then(Bean::getValueStr)
                        .shouldMatch(either(is("one")).or(is("1")))
                        .orFailWith(INVALID_COMBO_1),
                spec.<Integer, String>_2().when(Bean::getValue)
                        .matches(is(2))
                        .then(Bean::getValueStr)
                        .shouldMatch(either(is("two")).or(is("2")))
                        .orFailWith(INVALID_COMBO_2)))
                .prepare();

        val invalidBean1 = new Bean(1, "a", null, null);
        val failureResult1 = Runner.validateAndFailFast(invalidBean1, ValidationFailure::getValidationFailureForException, validationConfig);
        assertThat(failureResult1).contains(INVALID_COMBO_1);

        val invalidBean2 = new Bean(2, "b", null, null);
        val failureResult2 = Runner.validateAndFailFast(invalidBean2, ignore -> NONE, validationConfig);
        assertThat(failureResult2).contains(INVALID_COMBO_2);

        val validBean1 = new Bean(1, "one", null, null);
        val noneResult1 = Runner.validateAndFailFast(validBean1, ignore -> NONE, validationConfig);
        assertThat(noneResult1).isEmpty();

        val validBean2 = new Bean(2, "two", null, null);
        val noneResult2 = Runner.validateAndFailFast(validBean2, ValidationFailure::getValidationFailureForException, validationConfig);
        assertThat(noneResult2).isEmpty();
    }

    @Test
    void spec2WithName() {
        val invalidCombo1 = "invalidCombo1";
        val invalidCombo2 = "invalidCombo2";
        val validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(spec -> List.of(
                spec.<Integer, String>_2().nameForTest(invalidCombo1)
                        .orFailWith(INVALID_COMBO_1)
                        .when(Bean::getValue).matches(is(1))
                        .then(Bean::getValueStr).shouldMatch(either(is("one")).or(is("1"))),
                spec.<Integer, String>_2().nameForTest(invalidCombo2)
                        .orFailWith(INVALID_COMBO_2)
                        .when(Bean::getValue).matches(is(2))
                        .then(Bean::getValueStr).shouldMatch(either(is("two")).or(is("2")))))
                .prepare();
        val invalidBean1 = new Bean(1, "a", null, null);
        assertFalse(validationConfig.getSpecWithName(invalidCombo1).map(spec -> spec.test(invalidBean1)).orElse(true));

        val invalidBean2 = new Bean(2, "b", null, null);
        assertFalse(validationConfig.getSpecWithName(invalidCombo2).map(spec -> spec.test(invalidBean2)).orElse(true));

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

    private enum BillingTerm {
        OneTime, Month
    }

    @Value
    private static class Bean2 {
        BillingTerm bt;
        String valueStr;
    }
}
