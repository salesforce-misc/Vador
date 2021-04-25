package org.revcloud.vader.lift;

import consumer.bean.Member;
import consumer.bean.Parent;
import consumer.failure.ValidationFailure;
import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.Validator;

class AggregationValidatorLiftUtilTest {

    @Test
    void liftToParentValidationType() {
        val failure = Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        Validator<Member, ValidationFailure> childValidator = child -> failure;
        val liftedParentValidator = AggregationLiftUtil.liftToContainerValidatorType(childValidator, Parent::getMember, null, null);
        val toBeValidated = new Parent(0, null, new Member(0));
        Assertions.assertSame(failure, liftedParentValidator.unchecked().apply(Either.right(toBeValidated)));
    }

    @Test
    void liftToParentValidationTypeInvalidParent() {
        Validator<Member, ValidationFailure> childValidator = child -> Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        val liftedParentValidator = AggregationLiftUtil.liftToContainerValidatorType(childValidator, Parent::getMember, ValidationFailure.INVALID_PARENT, null);
        Assertions.assertEquals(Either.left(ValidationFailure.INVALID_PARENT), liftedParentValidator.unchecked().apply(Either.right(null)));
    }

    @Test
    void liftToParentValidationTypeInvalidChild() {
        Validator<Member, ValidationFailure> childValidator = child -> Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        val liftedParentValidator = AggregationLiftUtil.liftToContainerValidatorType(childValidator, Parent::getMember, null, ValidationFailure.INVALID_CHILD);
        Assertions.assertEquals(Either.left(ValidationFailure.INVALID_CHILD), liftedParentValidator.unchecked().apply(Either.right(new Parent(0, null, null))));
    }

    @Test
    void liftToParentValidationType2() {
        Validator<Member, ValidationFailure> childValidator = child -> child
                .flatMap(c -> c.getId() >= 0 ? child : Either.left(ValidationFailure.VALIDATION_FAILURE_1));
        val liftedParentValidator = AggregationLiftUtil.liftToContainerValidatorType(childValidator, Parent::getMember, null);
        final Member toBeValidatedMember = new Member(0);
        val toBeValidated = new Parent(0, null, toBeValidatedMember);
        Assertions.assertEquals(Either.right(toBeValidatedMember), liftedParentValidator.unchecked().apply(Either.right(toBeValidated)));
    }
    
    @Test
    void liftToParentValidationType2ForFailure() {
        val failure = Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        Validator<Member, ValidationFailure> childValidator = child -> child
                .flatMap(c -> c.getId() >= 0 ? child : failure);
        val liftedParentValidator = AggregationLiftUtil.liftToContainerValidatorType(childValidator, Parent::getMember, null);
        val toBeValidated = new Parent(0, null, new Member(-1));
        Assertions.assertSame(failure, liftedParentValidator.unchecked().apply(Either.right(toBeValidated)));
    }

    @Test
    void liftToParentValidationType2InvalidParent() {
        Validator<Member, ValidationFailure> childValidator = child -> Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        val liftedParentValidator = AggregationLiftUtil.liftToContainerValidatorType(childValidator, Parent::getMember, ValidationFailure.INVALID_PARENT);
        Assertions.assertEquals(Either.left(ValidationFailure.INVALID_PARENT), liftedParentValidator.unchecked().apply(Either.right(null)));
    }

    @Test
    void liftToParentValidationType2ThrowForNullChild() {
        Validator<Member, ValidationFailure> childValidator = child -> child
                .map(c -> c.getId() >= 0 ? ValidationFailure.NONE : ValidationFailure.VALIDATION_FAILURE_1);
        val liftedParentValidator = AggregationLiftUtil.liftToContainerValidatorType(childValidator, Parent::getMember, null);
        val validated = Either.<ValidationFailure, Parent>right(new Parent(0, null, null));
        Assertions.assertThrows(NullPointerException.class, () -> liftedParentValidator.apply(validated));
    }

}
