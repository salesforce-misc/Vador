package org.qtc.delphinus.dsl.lift;

import consumer.bean.Child;
import consumer.bean.Parent;
import consumer.failure.ValidationFailure;
import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qtc.delphinus.types.validators.Validator;

class ParentChildLiftDslTest {

    @Test
    void liftToParentValidationType() {
        val failure = Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        Validator<Child, ValidationFailure> childValidator = child -> failure;
        val liftedParentValidator = ParentChildLiftDsl.liftToParentValidationType(childValidator, Parent::getChild, null, null);
        val toBeValidated = new Parent(0, new Child(0));
        Assertions.assertSame(failure, liftedParentValidator.apply(Either.right(toBeValidated)));
    }

    @Test
    void liftToParentValidationTypeInvalidParent() {
        Validator<Child, ValidationFailure> childValidator = child -> Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        val liftedParentValidator = ParentChildLiftDsl.liftToParentValidationType(childValidator, Parent::getChild, ValidationFailure.INVALID_PARENT, null);
        Assertions.assertEquals(Either.left(ValidationFailure.INVALID_PARENT), liftedParentValidator.apply(Either.right(null)));
    }

    @Test
    void liftToParentValidationTypeInvalidChild() {
        Validator<Child, ValidationFailure> childValidator = child -> Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        val liftedParentValidator = ParentChildLiftDsl.liftToParentValidationType(childValidator, Parent::getChild, null, ValidationFailure.INVALID_CHILD);
        Assertions.assertEquals(Either.left(ValidationFailure.INVALID_CHILD), liftedParentValidator.apply(Either.right(new Parent(0, null))));
    }

    @Test
    void liftToParentValidationType2() {
        Validator<Child, ValidationFailure> childValidator = child -> child
                .flatMap(c -> c.getId() >= 0 ? child : Either.left(ValidationFailure.VALIDATION_FAILURE_1));
        val liftedParentValidator = ParentChildLiftDsl.liftToParentValidationType(childValidator, Parent::getChild, null);
        final Child toBeValidatedChild = new Child(0);
        val toBeValidated = new Parent(0, toBeValidatedChild);
        Assertions.assertEquals(Either.right(toBeValidatedChild), liftedParentValidator.apply(Either.right(toBeValidated)));
    }
    
    @Test
    void liftToParentValidationType2ForFailure() {
        val failure = Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        Validator<Child, ValidationFailure> childValidator = child -> child
                .flatMap(c -> c.getId() >= 0 ? child : failure);
        val liftedParentValidator = ParentChildLiftDsl.liftToParentValidationType(childValidator, Parent::getChild, null);
        val toBeValidated = new Parent(0, new Child(-1));
        Assertions.assertSame(failure, liftedParentValidator.apply(Either.right(toBeValidated)));
    }

    @Test
    void liftToParentValidationType2InvalidParent() {
        Validator<Child, ValidationFailure> childValidator = child -> Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        val liftedParentValidator = ParentChildLiftDsl.liftToParentValidationType(childValidator, Parent::getChild, ValidationFailure.INVALID_PARENT);
        Assertions.assertEquals(Either.left(ValidationFailure.INVALID_PARENT), liftedParentValidator.apply(Either.right(null)));
    }

    @Test
    void liftToParentValidationType2ThrowForNullChild() {
        Validator<Child, ValidationFailure> childValidator = child -> child
                .map(c -> c.getId() >= 0 ? ValidationFailure.NONE : ValidationFailure.VALIDATION_FAILURE_1);
        val liftedParentValidator = ParentChildLiftDsl.liftToParentValidationType(childValidator, Parent::getChild, null);
        val validated = Either.<ValidationFailure, Parent>right(new Parent(0, null));
        Assertions.assertThrows(NullPointerException.class, () -> liftedParentValidator.apply(validated));
    }

}
