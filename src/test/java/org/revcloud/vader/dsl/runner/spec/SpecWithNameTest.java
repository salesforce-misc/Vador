package org.revcloud.vader.dsl.runner.spec;

import consumer.failure.ValidationFailure;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.dsl.runner.ValidationConfig;

import java.util.List;

import static consumer.failure.ValidationFailure.INVALID_COMBO_1;
import static consumer.failure.ValidationFailure.INVALID_COMBO_2;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;

class SpecWithNameTest {
    @Test
    void spec2WithName() {
        final var invalidCombo1 = "invalidCombo1";
        final var invalidCombo2 = "invalidCombo2";
        ValidationConfig<RunnerDslWithBiSpecTest.Bean, ValidationFailure> validationConfig =
                ValidationConfig.<RunnerDslWithBiSpecTest.Bean, ValidationFailure>toValidate().withSpecs(spec -> List.of(
                        spec._2.nameForTest(invalidCombo1)
                                .orFailWith(INVALID_COMBO_1)
                                .when(RunnerDslWithBiSpecTest.Bean::getValue).is(1)
                                .then(RunnerDslWithBiSpecTest.Bean::getValueStr).shouldBe(either(is("one")).or(is("1"))).done(),
                        spec._2.nameForTest(invalidCombo2)
                                .orFailWith(INVALID_COMBO_2)
                                .when(RunnerDslWithBiSpecTest.Bean::getValue).is(2)
                                .then(RunnerDslWithBiSpecTest.Bean::getValueStr).shouldBe(either(is("two")).or(is("2"))).done()))
                        .prepare();
        val invalidBean1 = new RunnerDslWithBiSpecTest.Bean(1, "a", null, null);
        Assertions.assertFalse(validationConfig.getSpecWithName(invalidCombo1).map(spec -> spec.test(invalidBean1)).orElse(true));

        val invalidBean2 = new RunnerDslWithBiSpecTest.Bean(2, "b", null, null);
        Assertions.assertFalse(validationConfig.getSpecWithName(invalidCombo2).map(spec -> spec.test(invalidBean2)).orElse(true));
        
        val validBean1 = new RunnerDslWithBiSpecTest.Bean(1, "one", null, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidCombo1).map(spec -> spec.test(validBean1)).orElse(false));
        
        val validBean2 = new RunnerDslWithBiSpecTest.Bean(2, "two", null, null);
        Assertions.assertTrue(validationConfig.getSpecWithName(invalidCombo2).map(spec -> spec.test(validBean2)).orElse(false));
    }
}
