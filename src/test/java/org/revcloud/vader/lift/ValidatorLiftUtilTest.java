package org.revcloud.vader.lift;

import consumer.bean.Parent;
import consumer.failure.ValidationFailure;
import io.vavr.control.Either;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.Validator;

class ValidatorLiftUtilTest {

  @Test
  void liftSimpleForFailure() {
    Validator<Parent, ValidationFailure> validator =
        parent -> ValidationFailure.VALIDATION_FAILURE_1;
    final var liftedValidator =
        ValidatorLiftUtil.liftToEtr(validator, ValidationFailure.NONE);
    Assertions.assertEquals(
        liftedValidator.unchecked().apply(Either.right(new Parent(0, null, null))),
        Either.left(ValidationFailure.VALIDATION_FAILURE_1));
  }

  @Test
  void liftSimpleForNoFailure() {
    Validator<Parent, ValidationFailure> validator = parent -> ValidationFailure.NONE;
    final var liftedValidator =
        ValidatorLiftUtil.liftToEtr(validator, ValidationFailure.NONE);
    final Parent toBeValidated = new Parent(0, null, null);
    Assertions.assertEquals(
        liftedValidator.unchecked().apply(Either.right(toBeValidated)),
        Either.right(toBeValidated));
  }
}
