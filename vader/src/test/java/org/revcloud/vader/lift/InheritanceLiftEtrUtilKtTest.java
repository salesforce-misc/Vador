package org.revcloud.vader.lift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.revcloud.vader.lift.InheritanceLiftEtrUtil.liftAllToChildValidatorType;
import static sample.consumer.failure.ValidationFailure.NONE;
import static sample.consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;

import io.vavr.Tuple;
import io.vavr.control.Either;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.Vader;
import org.revcloud.vader.runner.ValidationConfig;
import org.revcloud.vader.types.Validator;
import org.revcloud.vader.types.ValidatorEtr;
import sample.consumer.failure.ValidationFailure;

class InheritanceLiftEtrUtilKtTest {

  @Test
  void liftParentToChildValidatorTypeTest() {
    final Validator<Parent, ValidationFailure> v1 = ignore -> NONE;
    final Validator<Child, ValidationFailure> v2 = ignore -> UNKNOWN_EXCEPTION;
    final var validationConfig =
        ValidationConfig.<Child, ValidationFailure>toValidate()
            .withValidators(Tuple.of(List.of(v1, v2), NONE))
            .prepare();
    final var result = Vader.validateAndFailFast(new Child(), validationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Test
  void liftParentToChildValidatorEtrTypeTest() {
    final ValidatorEtr<Parent, ValidationFailure> v1 = ignore -> Either.right(NONE);
    final ValidatorEtr<Parent, ValidationFailure> v2 = ignore -> Either.right(NONE);
    final ValidatorEtr<Child, ValidationFailure> v3 = ignore -> Either.left(UNKNOWN_EXCEPTION);
    final var validationConfig =
        ValidationConfig.<Child, ValidationFailure>toValidate()
            .withValidatorEtrs(liftAllToChildValidatorType(List.of(v1, v2)))
            .withValidatorEtr(v3)
            .prepare();
    final var result = Vader.validateAndFailFast(new Child(), validationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  private abstract class Parent {}

  @Data
  @EqualsAndHashCode(callSuper = true)
  private class Child extends Parent {}
}
