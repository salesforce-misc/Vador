package org.revcloud.vader.dsl.runner.spec;

import consumer.failure.ValidationFailure;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.dsl.runner.ValidationConfig;

import java.util.Map;
import java.util.Set;

import static consumer.failure.ValidationFailure.INVALID_COMBO_1;
import static consumer.failure.ValidationFailure.getFailureWithParams;
import static consumer.failure.ValidationFailureMessage.MSG_WITH_PARAMS;

class Spec3Test {
    @Test
    void spec3() {
        val invalidCombo = "invalidCombo";
        val validComboMap = Map.of(
                1, Set.of("1", "one"),
                2, Set.of("2", "two")
        );
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate().withSpec(spec ->
                        spec._3().nameForTest(invalidCombo)
                                .orFailWith(INVALID_COMBO_1)
                                .when(Bean::getValue)
                                .then(Bean::getValueStr)
                                .shouldMatch(validComboMap)
                                .orFailWithFn((value, valueStr) -> getFailureWithParams(MSG_WITH_PARAMS, new Object[]{value, valueStr})))
                        .prepare();

        val invalidBean1 = new Bean(1, "a", null, null);
        Assertions.assertFalse(validationConfig.getSpecWithName(invalidCombo).map(spec -> spec.test(invalidBean1)).orElse(true));

        val invalidBean2 = new Bean(2, "b", null, null);
        Assertions.assertFalse(validationConfig.getSpecWithName(invalidCombo).map(spec -> spec.test(invalidBean2)).orElse(true));

        val validBean1 = new Bean(1, "one", null, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidCombo).map(spec -> spec.test(validBean1)).orElse(false));

        val validBean2 = new Bean(2, "two", null, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidCombo).map(spec -> spec.test(validBean2)).orElse(false));
    }

    @Value
    private static class Bean {
        Integer value;
        String valueStr;
        Integer dependentValue1;
        Integer dependentValue2;
    }
}
