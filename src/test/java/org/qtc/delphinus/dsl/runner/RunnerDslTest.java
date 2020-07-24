package org.qtc.delphinus.dsl.runner;

import consumer.bean.Parent;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qtc.delphinus.dsl.runner.RunnerDsl;
import org.qtc.delphinus.types.validators.Validator;
import org.qtc.delphinus.types.validators.simple.SimpleValidator;

import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;

class RunnerDslTest {

    @Test
    void FailFastWithFirstFailure() {
        val result = validateAndFailFast(
                NONE,
                NOTHING_TO_VALIDATE,
                new Parent(0, null),
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
                none
        );
    }

    @Test
    void FailFastWithFirstFailureForSimpleValidators() {
        val result = validateAndFailFastForSimpleValidators(
                NONE,
                NOTHING_TO_VALIDATE,
                new Parent(0, null),
                UNKNOWN_EXCEPTION
        );
        Assertions.assertSame(UNKNOWN_EXCEPTION, result);
    }

    private static <ParentT, FailureT> FailureT validateAndFailFastForSimpleValidators(
            FailureT none,
            FailureT nothingToValidate,
            ParentT parentToValidate,
            FailureT firstValidationFailure
    ) {

        SimpleValidator<ParentT, FailureT> v1 = parent -> none;
        SimpleValidator<ParentT, FailureT> v2 = parent -> none;
        SimpleValidator<ParentT, FailureT> v3 = parent -> firstValidationFailure;

        val validationList = List.of(v1, v2, v3);
        return RunnerDsl.validateAndFailFastForSimpleValidators(
                parentToValidate,
                validationList,
                nothingToValidate,
                none
        );
    }
    
}

