package org.revcloud.vader.runner.spec;

import consumer.failure.ValidationFailure;
import io.vavr.Function1;
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
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;

class Spec_11_1n_Test {
    // TODO 19/04/21 gopala.akshintala: Replicate these tests for batch 
    private static <ValidatableT, FailureT> FailureT validateAndFailFastForSimpleValidatorsWithConfig(
            FailureT none,
            FailureT nothingToValidate,
            ValidatableT validatable,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return Runner.validateAndFailFastForSimpleValidators(
                validatable,
                io.vavr.collection.List.empty(), // TODO 12/04/21 gopala.akshintala: migrate to use java immutable collection 
                nothingToValidate,
                none,
                throwableMapper,
                validationConfig);
    }

    @Test
    void failFastWithInvalidIdForSimpleValidators() {
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(spec -> List.of(
                        spec._11_1n().orFailWith(INVALID_COMBO_1)
                                .when(Bean::getValue).is(1)
                                .then(Bean::getValueStr)
                                .shouldBe(either(is("one")).or(is("1"))),
                        spec._11_1n().orFailWith(INVALID_COMBO_2)
                                .when(Bean::getValue).is(2)
                                .then(Bean::getValueStr).shouldBe(either(is("two")).or(is("2")))))
                        .prepare();

        val invalidBean1 = new Bean(1, "a", null, null);
        val failureResult1 = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, invalidBean1, ignore -> NONE, validationConfig);
        Assertions.assertEquals(INVALID_COMBO_1, failureResult1);

        val invalidBean2 = new Bean(2, "b", null, null);
        val failureResult2 = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, invalidBean2, ignore -> NONE, validationConfig);
        Assertions.assertEquals(INVALID_COMBO_2, failureResult2);

        val validBean1 = new Bean(1, "one", null, null);
        val noneResult1 = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, validBean1, ignore -> NONE, validationConfig);
        Assertions.assertEquals(NONE, noneResult1);

        val validBean2 = new Bean(2, "two", null, null);
        val noneResult2 = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, validBean2, ValidationFailure::getValidationFailureForException, validationConfig);
        Assertions.assertEquals(NONE, noneResult2);
    }

    @Test
    void spec2WithName() {
        val invalidCombo1 = "invalidCombo1";
        val invalidCombo2 = "invalidCombo2";
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(spec -> List.of(
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


