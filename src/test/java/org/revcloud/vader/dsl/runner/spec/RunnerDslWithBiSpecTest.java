package org.revcloud.vader.dsl.runner.spec;

import consumer.failure.ValidationFailure;
import io.vavr.Function1;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.dsl.runner.RunnerDsl;
import org.revcloud.vader.dsl.runner.ValidationConfig;

import java.util.List;

import static consumer.failure.ValidationFailure.INVALID_COMBO_1;
import static consumer.failure.ValidationFailure.INVALID_COMBO_2;
import static consumer.failure.ValidationFailure.NONE;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;

class RunnerDslWithBiSpecTest {
    @Test
    void failFastWithInvalidIdForSimpleValidators() {
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(spec -> List.of(
                        spec._2.orFailWith(INVALID_COMBO_1)
                                .when(Bean::getValue).is(1)
                                .then(Bean::getValueStr).shouldBe(either(is("one")).or(is("1"))).done(),
                        spec._2.orFailWith(INVALID_COMBO_2)
                                .when(Bean::getValue).is(2)
                                .then(Bean::getValueStr).shouldBe(either(is("two")).or(is("2"))).done()))
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
    void failFastWithInvalidIdForSimpleValidators2() {
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(spec -> List.of(
                        spec._2.orFailWith(INVALID_COMBO_1)
                                .when(Bean::getValueStr).is(null)
                                .then(Bean::getValue).matchesField(Bean::getDependentValue1).done()))
                        .prepare();
        val invalidBean = new Bean(1, null, 2, 1);
        val failureResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, invalidBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(INVALID_COMBO_1, failureResult);

        val validBean = new Bean(1, null, 1, 2);
        val noneResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, validBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(NONE, noneResult);
    }

    @Test
    void failFastWithInvalidIdForSimpleValidators3() {
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(spec -> List.of(
                        spec._2.orFailWith(INVALID_COMBO_1)
                                .when(Bean::getValueStr).is(null)
                                .then(Bean::getValue).matchesField(Bean::getDependentValue1).orMatchesField(Bean::getDependentValue2).done()))
                        .prepare();
        val invalidBean = new Bean(1, null, 2, 2);
        val failureResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, invalidBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(INVALID_COMBO_1, failureResult);

        val validBean = new Bean(1, null, 2, 1);
        val noneResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, validBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(NONE, noneResult);
    }

    // TODO 19/04/21 gopala.akshintala: Replicate these tests for batch 
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
        String valueStr;
        Integer dependentValue1;
        Integer dependentValue2;
    }
}


