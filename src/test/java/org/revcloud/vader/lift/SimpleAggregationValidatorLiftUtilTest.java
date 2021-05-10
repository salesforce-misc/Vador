package org.revcloud.vader.lift;

import consumer.bean.Container;
import consumer.bean.Member;
import consumer.failure.ValidationFailure;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.SimpleValidator;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimpleAggregationValidatorLiftUtilTest {

    @Test
    void liftToContainerValidationTypeWithNullMember() {
        SimpleValidator<Member, ValidationFailure> childValidator = child -> ValidationFailure.NONE;
        final var liftedContainerValidation = AggregationLiftSimpleUtil.liftToContainerValidatorType(
                childValidator,
                Container::getMember,
                ValidationFailure.INVALID_CONTAINER,
                ValidationFailure.INVALID_MEMBER);
        assertSame(ValidationFailure.INVALID_MEMBER, liftedContainerValidation.unchecked().apply(new Container(0, null)));
    }

    @Test
    void liftToContainerValidationTypeWithNullContainer() {
        SimpleValidator<Member, ValidationFailure> childValidator = child -> ValidationFailure.NONE;
        final var liftedContainerValidation = AggregationLiftSimpleUtil.liftToContainerValidatorType(
                        childValidator,
                        Container::getMember,
                        ValidationFailure.INVALID_CONTAINER,
                        ValidationFailure.INVALID_MEMBER);
        assertSame(ValidationFailure.INVALID_CONTAINER, liftedContainerValidation.unchecked().apply(null));
    }

    @Test
    void liftToContainerValidationType() {
        SimpleValidator<Member, ValidationFailure> childValidator = child -> ValidationFailure.NONE;
        final var liftedContainerValidation = AggregationLiftSimpleUtil.liftToContainerValidatorType(
                        childValidator,
                        Container::getMember,
                        ValidationFailure.INVALID_CONTAINER,
                        ValidationFailure.INVALID_MEMBER);
        assertSame(ValidationFailure.NONE, liftedContainerValidation.unchecked().apply(new Container(0, new Member(0))));
    }
}
