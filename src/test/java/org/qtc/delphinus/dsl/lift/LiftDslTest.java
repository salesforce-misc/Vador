package org.qtc.delphinus.dsl.lift;

import consumer.bean.Parent;
import consumer.failure.ValidationFailure;
import consumer.failure.ValidationFailureMessage;
import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qtc.delphinus.types.validators.simple.SimpleThrowableValidator;
import org.qtc.delphinus.types.validators.simple.SimpleValidator;

class LiftDslTest {

    @Test
    void liftSimpleForFailure() {
        SimpleValidator<Parent, ValidationFailure> simpleValidator = parent -> ValidationFailure.VALIDATION_FAILURE_1;
        val liftedValidator = LiftDsl.liftSimple(simpleValidator, ValidationFailure.NONE);
        Assertions.assertEquals(liftedValidator.apply(Either.right(new Parent(0, null))),
                Either.left(ValidationFailure.VALIDATION_FAILURE_1));
    }

    @Test
    void liftSimpleForNoFailure() {
        SimpleValidator<Parent, ValidationFailure> simpleValidator = parent -> ValidationFailure.NONE;
        val liftedValidator = LiftDsl.liftSimple(simpleValidator, ValidationFailure.NONE);
        final Parent toBeValidated = new Parent(0, null);
        Assertions.assertEquals(liftedValidator.apply(Either.right(toBeValidated)),
                Either.right(toBeValidated));
    }

    @Test
    void liftSimpleThrowableForException() {
        SimpleThrowableValidator<Parent, ValidationFailure> simpleValidator = parent -> {
            throw new RuntimeException();
        };
        val liftedValidator = LiftDsl.liftSimpleThrowable(simpleValidator, ValidationFailure.NONE, 
                ValidationFailure::getValidationFailureForException);
        Assertions.assertEquals(liftedValidator.apply(Either.right(new Parent(0, null))),
                Either.left(new ValidationFailure(ValidationFailureMessage.UNKNOWN_EXCEPTION)));
    }

    @Test
    void liftSimpleThrowableForFailure() {
        SimpleThrowableValidator<Parent, ValidationFailure> simpleValidator = parent -> ValidationFailure.VALIDATION_FAILURE_1;
        val liftedValidator = LiftDsl.liftSimpleThrowable(simpleValidator, ValidationFailure.NONE,
                ValidationFailure::getValidationFailureForException);
        Assertions.assertEquals(liftedValidator.apply(Either.right(new Parent(0, null))),
                Either.left(ValidationFailure.VALIDATION_FAILURE_1));
    }

    @Test
    void liftSimpleThrowableForNoFailure() {
        SimpleThrowableValidator<Parent, ValidationFailure> simpleValidator = parent -> ValidationFailure.NONE;
        val liftedValidator = LiftDsl.liftSimpleThrowable(simpleValidator, ValidationFailure.NONE,
                ValidationFailure::getValidationFailureForException);
        final Parent toBeValidated = new Parent(0, null);
        Assertions.assertEquals(liftedValidator.apply(Either.right(toBeValidated)), Either.right(toBeValidated));
    }
}
