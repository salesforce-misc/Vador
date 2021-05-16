package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import io.vavr.control.Either;
import java.util.List;
import lombok.Value;
import org.junit.jupiter.api.Test;

class RunnerFailFastTest {

  @Test
  void failFastWithFirstFailure() {
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .withValidators(
                List.of(
                    bean -> Either.right(NONE),
                    bean -> Either.right(NONE),
                    bean -> Either.left(UNKNOWN_EXCEPTION)))
            .prepare();
    final var result =
        Runner.validateAndFailFast(
            new Bean(0), ValidationFailure::getValidationFailureForException, validationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Test
  void failFastWithFirstFailureForSimpleValidators() {
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .withSimpleValidators(
                Tuple.of(List.of(bean -> NONE, bean -> NONE, bean -> UNKNOWN_EXCEPTION), NONE))
            .prepare();
    final var result =
        Runner.validateAndFailFast(
            new Bean(0), ValidationFailure::getValidationFailureForException, validationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Value
  private static class Bean {
    int id;
  }
}
