package org.revcloud.vader.lift;

import consumer.bean.Member;
import consumer.bean.Container;
import consumer.failure.ValidationFailure;
import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.Validator;

class AggregationValidatorLiftUtilTest {

    @Test
    void liftToContainerValidationType() {
        val failure = Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        Validator<Member, ValidationFailure> memberValidator = member -> failure;
        val liftedContainerValidator = AggregationLiftUtil.liftToContainerValidatorType(memberValidator, Container::getMember, null, null);
        val toBeValidated = new Container(0, new Member(0));
        Assertions.assertSame(failure, liftedContainerValidator.unchecked().apply(Either.right(toBeValidated)));
    }

    @Test
    void liftToContainerValidationTypeInvalidContainer() {
        Validator<Member, ValidationFailure> memberValidator = member -> Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        val liftedContainerValidator = AggregationLiftUtil.liftToContainerValidatorType(memberValidator, Container::getMember, ValidationFailure.INVALID_CONTAINER, null);
        Assertions.assertEquals(Either.left(ValidationFailure.INVALID_CONTAINER), liftedContainerValidator.unchecked().apply(Either.right(null)));
    }

    @Test
    void liftToContainerValidationTypeInvalidMember() {
        Validator<Member, ValidationFailure> memberValidator = member -> Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        val liftedContainerValidator = AggregationLiftUtil.liftToContainerValidatorType(memberValidator, Container::getMember, null, ValidationFailure.INVALID_MEMBER);
        Assertions.assertEquals(Either.left(ValidationFailure.INVALID_MEMBER), liftedContainerValidator.unchecked().apply(Either.right(new Container(0, null))));
    }
}
