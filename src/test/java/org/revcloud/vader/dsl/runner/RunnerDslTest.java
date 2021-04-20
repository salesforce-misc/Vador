package org.revcloud.vader.dsl.runner;

import consumer.bean.Parent;
import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;

class RunnerDslTest {

    @Test
    void failFastWithFirstFailure() {
        val result = validateAndFailFast(
                NONE,
                NOTHING_TO_VALIDATE,
                new Parent(0, null, null),
                UNKNOWN_EXCEPTION
        );
        Assertions.assertSame(UNKNOWN_EXCEPTION, result);
    }

    private static <ParentT, FailureT> FailureT validateAndFailFast(
            FailureT none,
            FailureT nothingToValidate,
            ParentT parentToValidate,
            FailureT firstValidationFailure
    ) {

        Validator<ParentT, FailureT> v1 = parent -> Either.right(none);
        Validator<ParentT, FailureT> v2 = parent -> Either.right(none);
        Validator<ParentT, FailureT> v3 = parent -> Either.left(firstValidationFailure);

        val validationList = List.of(v1, v2, v3);
        return RunnerDsl.validateAndFailFast(
                parentToValidate,
                validationList,
                nothingToValidate,
                none,
                throwable -> none
        );
    }

    @Test
    void failFastWithFirstFailureForSimpleValidators() {
        val validatable = new Parent(0, null, null);
        val result = validateAndFailFastForSimpleValidators(
                NONE,
                NOTHING_TO_VALIDATE,
                validatable,
                UNKNOWN_EXCEPTION,
                throwable -> UNKNOWN_EXCEPTION
        );
        Assertions.assertSame(UNKNOWN_EXCEPTION, result);
    }

    private static <ValidatableT, FailureT> FailureT validateAndFailFastForSimpleValidators(
            FailureT none,
            FailureT nothingToValidate,
            ValidatableT validatable,
            FailureT firstValidationFailure,
            Function1<Throwable, FailureT> throwableMapper
    ) {

        SimpleValidator<ValidatableT, FailureT> v1 = parent -> none;
        SimpleValidator<ValidatableT, FailureT> v2 = parent -> none;
        SimpleValidator<ValidatableT, FailureT> v3 = parent -> firstValidationFailure;

        val validationList = List.of(v1, v2, v3);
        return RunnerDsl.validateAndFailFastForSimpleValidators(
                validatable,
                validationList,
                nothingToValidate,
                none,
                throwableMapper
        );
    }
}

