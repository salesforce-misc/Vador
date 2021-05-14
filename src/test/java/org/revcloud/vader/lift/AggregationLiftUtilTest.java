package org.revcloud.vader.lift;

import consumer.bean.Member;
import consumer.bean.Container;
import consumer.failure.ValidationFailure;
import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.Validator;

import static org.revcloud.vader.lift.AggregationLiftUtil.liftToContainerValidatorType;

class AggregationLiftUtilTest {

    @Test
    void liftToContainerValidationType() {
        final var failure = Either.left(ValidationFailure.VALIDATION_FAILURE_1);
        Validator<? super Member, ? extends ValidationFailure> memberValidator = member -> failure;
        final var liftedContainerValidator = liftToContainerValidatorType(memberValidator, Container::getMember);
        final var toBeValidated = new Container(0, new Member(0));
        Assertions.assertSame(failure, liftedContainerValidator.unchecked().apply(Either.right(toBeValidated)));
    }
}
