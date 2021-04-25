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
import static consumer.failure.ValidationFailure.NONE;

class Spec_11_1nf_Test {

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
    void failFastWithInvalidIdForSimpleValidators2() {
        ValidationConfig<Bean, ValidationFailure> validationConfig =
                ValidationConfig.<Bean, ValidationFailure>toValidate().withSpecs(spec -> List.of(
                        spec._11_1nf().orFailWith(INVALID_COMBO_1)
                                .when(Bean::getValueStr).is(null)
                                .then(Bean::getValue)
                                .shouldMatchField(Bean::getDependentValue1)))
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
                        spec._11_1nf().orFailWith(INVALID_COMBO_1)
                                .when(Bean::getValueStr).is(null)
                                .then(Bean::getValue)
                                .shouldMatchAnyField(List.of(Bean::getDependentValue1, Bean::getDependentValue2))))
                        .prepare();
        val invalidBean = new Bean(1, null, 2, 2);
        val failureResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, invalidBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(INVALID_COMBO_1, failureResult);

        val validBean = new Bean(1, null, 2, 1);
        val noneResult = validateAndFailFastForSimpleValidatorsWithConfig(NONE, NONE, validBean, ignore -> NONE, validationConfig);
        Assertions.assertEquals(NONE, noneResult);
    }

    @Value
    private static class Bean {
        Integer value;
        String valueStr;
        Integer dependentValue1;
        Integer dependentValue2;
    }
}


