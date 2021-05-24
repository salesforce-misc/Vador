package org.revcloud.vader.lift;

import static org.revcloud.vader.lift.AggregationLiftEtrUtil.liftToContainerValidatorType;

import consumer.bean.Container;
import consumer.bean.Member;
import consumer.failure.ValidationFailure;
import io.vavr.control.Either;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.ValidatorEtr;

class AggregationLiftEtrUtilTest {

  @Test
  void liftToContainerValidationType() {
    final var failure = Either.left(ValidationFailure.VALIDATION_FAILURE_1);
    ValidatorEtr<? super Member, ? extends ValidationFailure> memberValidator = member -> failure;
    final var liftedContainerValidator =
        liftToContainerValidatorType(memberValidator, Container::getMember);
    final var toBeValidated = new Container(0, new Member(0));
    Assertions.assertSame(
        failure, liftedContainerValidator.unchecked().apply(Either.right(toBeValidated)));
  }
}
