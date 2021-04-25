package org.revcloud.vader.runner;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import io.vavr.control.Either;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;

class RunnerTest {

    @Test
    void failFastWithFirstFailure() {
        val validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withValidators(List.of(
                bean -> Either.right(NONE),
                bean -> Either.right(NONE),
                bean -> Either.left(UNKNOWN_EXCEPTION)
        )).prepare();
        val result = Runner.validateAndFailFast(
                new Bean(0),
                NOTHING_TO_VALIDATE,
                ValidationFailure::getValidationFailureForException,
                validationConfig
        );
        assertThat(result).contains(UNKNOWN_EXCEPTION);
    }

    @Test
    void failFastWithFirstFailureForSimpleValidators() {
        val validationConfig = ValidationConfig.<Bean, ValidationFailure>toValidate().withSimpleValidatorsOrFailWith(Tuple.of(List.of(
                bean -> NONE,
                bean -> NONE,
                bean -> UNKNOWN_EXCEPTION
        ), NONE)).prepare();
        val result = Runner.validateAndFailFast(
                new Bean(0),
                NOTHING_TO_VALIDATE,
                ValidationFailure::getValidationFailureForException,
                validationConfig
        );
        assertThat(result).contains(UNKNOWN_EXCEPTION);
    }

    @Value
    private static class Bean {
        int id;
    }
}

