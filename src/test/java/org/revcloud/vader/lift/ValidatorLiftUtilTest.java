package org.revcloud.vader.lift;

import consumer.bean.Parent;
import consumer.failure.ValidationFailure;
import io.vavr.control.Either;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.SimpleValidator;

class ValidatorLiftUtilTest {

  @Test
  void liftSimpleForFailure() {
    SimpleValidator<Parent, ValidationFailure> simpleValidator =
        parent -> ValidationFailure.VALIDATION_FAILURE_1;
    final var liftedValidator =
        ValidatorLiftUtil.liftSimple(simpleValidator, ValidationFailure.NONE);
    Assertions.assertEquals(
        liftedValidator.unchecked().apply(Either.right(new Parent(0, null, null))),
        Either.left(ValidationFailure.VALIDATION_FAILURE_1));
  }

  @Test
  void liftSimpleForNoFailure() {
    SimpleValidator<Parent, ValidationFailure> simpleValidator = parent -> ValidationFailure.NONE;
    final var liftedValidator =
        ValidatorLiftUtil.liftSimple(simpleValidator, ValidationFailure.NONE);
    final Parent toBeValidated = new Parent(0, null, null);
    Assertions.assertEquals(
        liftedValidator.unchecked().apply(Either.right(toBeValidated)),
        Either.right(toBeValidated));
  }
}
