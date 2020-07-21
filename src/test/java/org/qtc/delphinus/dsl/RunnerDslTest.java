package org.qtc.delphinus.dsl;

import consumer.failure.ValidationFailure;
import consumer.representation.Parent;
import io.vavr.collection.List;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qtc.delphinus.types.validators.simple.SimpleValidator;

import java.util.function.Supplier;

class RunnerDslTest {

    @Test
    void FailFastWithFirstFailure() {
        val result = validateAndFailFastForSimpleValidators(
                () -> ValidationFailure.NONE,
                () -> ValidationFailure.NOTHING_TO_VALIDATE,
                () -> ValidationFailure.UNKNOWN_EXCEPTION,
                Parent::new
        );
        Assertions.assertSame(ValidationFailure.UNKNOWN_EXCEPTION, result);
    }

    private static <ParentT, FailureT> FailureT validateAndFailFastForSimpleValidators(
            Supplier<FailureT> getNone,
            Supplier<FailureT> getNothingToValidate,
            Supplier<ParentT> getParentToValidate,
            Supplier<FailureT> getFirstValidationFailure
    ) {
        val none = getNone.get();
        val nothingToValidate = getNothingToValidate.get();
        val firstValidationFailure = getFirstValidationFailure.get();

        SimpleValidator<ParentT, FailureT> v1 = parent -> none;
        SimpleValidator<ParentT, FailureT> v2 = parent -> none;
        SimpleValidator<ParentT, FailureT> v3 = parent -> firstValidationFailure;

        val validationList = List.of(v1, v2, v3);
        return RunnerDsl.validateAndFailFastForSimpleValidators(
                getParentToValidate.get(),
                validationList,
                nothingToValidate,
                none
        );
    }
}

