package org.revcloud.vader.dsl.runner;

import consumer.failure.ValidationFailure;
import io.vavr.Function1;

import java.util.Collection;
import java.util.List;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static consumer.failure.ValidationFailure.INVALID_COMBO;
import static consumer.failure.ValidationFailure.NONE;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;

class RunnerDslWithBiSpecTest {
    @Test
    void failFastWithInvalidIdForSimpleValidators() {
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(List.of(
                        BiSpec.<Bean, ValidationFailure>check().orFailWith(INVALID_COMBO)
                                .when(Bean::getValue).is(1)
                                .then(Bean::getValueStr).shouldBe(either(is("one")).or(is("1")))))
                        .prepare();
        val invalidBean = new Bean(1, "a", null, null);
        val failureResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, invalidBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(INVALID_COMBO, failureResult);

        val validBean = new Bean(1, "one", null, null);
        val noneResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, validBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(NONE, noneResult);
    }

    @Test
    void failFastWithInvalidIdForSimpleValidators2() {
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(List.of(
                        BiSpec.<Bean, ValidationFailure>check().orFailWith(INVALID_COMBO)
                                .when(Bean::getValueStr).is(null)
                                .then(Bean::getValue).matchesField(Bean::getDependentValue1)))
                        .prepare();
        val invalidBean = new Bean(1, null, 2, 1);
        val failureResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, invalidBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(INVALID_COMBO, failureResult);

        val validBean = new Bean(1, null, 1, 2);
        val noneResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, validBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(NONE, noneResult);
    }

    @Test
    void failFastWithInvalidIdForSimpleValidators3() {
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(List.of(
                        BiSpec.<Bean, ValidationFailure>check().orFailWith(INVALID_COMBO)
                                .when(Bean::getValueStr).is(null)
                                .then(Bean::getValue).matchesField(Bean::getDependentValue1).orMatchesField(Bean::getDependentValue2)))
                        .prepare();
        val invalidBean = new Bean(1, null, 2, 2);
        val failureResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, invalidBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(INVALID_COMBO, failureResult);

        val validBean = new Bean(1, null, 2, 1);
        val noneResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, validBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(NONE, noneResult);
    }

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


