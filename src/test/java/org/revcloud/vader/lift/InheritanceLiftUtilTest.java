package org.revcloud.vader.lift;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import io.vavr.control.Either;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.Runner;
import org.revcloud.vader.runner.ValidationConfig;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import java.util.List;

import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.revcloud.vader.lift.InheritanceLiftUtil.liftToChildValidatorType;

class InheritanceLiftUtilTest {
    @Test
    void liftSimpleParentToChildValidatorTypeTest() {
        final SimpleValidator<Parent, ValidationFailure> v1 = ignore -> NONE;
        final SimpleValidator<Child, ValidationFailure> v2 = ignore -> UNKNOWN_EXCEPTION;
        final var validationConfig = ValidationConfig.<Child, ValidationFailure>toValidate()
                .withSimpleValidators(Tuple.of(List.of(v1, v2), NONE)).prepare();
        final var result = Runner.validateAndFailFast(
                new Child(),
                ValidationFailure::getValidationFailureForException,
                validationConfig);
        assertThat(result).contains(UNKNOWN_EXCEPTION);
    }

    @Test
    void liftParentToChildValidatorTypeTest() {
        final Validator<Parent, ValidationFailure> v1 = ignore -> Either.right(NONE);
        final Validator<Child, ValidationFailure> v2 = ignore -> Either.left(UNKNOWN_EXCEPTION);
        final var validationConfig = ValidationConfig.<Child, ValidationFailure>toValidate()
                .withValidators(List.of(liftToChildValidatorType(v1), v2)).prepare();
        final var result = Runner.validateAndFailFast(
                new Child(),
                ValidationFailure::getValidationFailureForException,
                validationConfig);
        assertThat(result).contains(UNKNOWN_EXCEPTION);
    }

    private abstract class Parent {
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private class Child extends Parent {
    }
}
