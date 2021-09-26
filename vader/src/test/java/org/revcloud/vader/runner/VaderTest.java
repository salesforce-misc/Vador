package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;
import static consumer.failure.ValidationFailure.VALIDATION_FAILURE_1;
import static consumer.failure.ValidationFailure.VALIDATION_FAILURE_2;
import static org.assertj.core.api.Assertions.assertThat;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import io.vavr.control.Either;
import java.util.List;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.Validator;
import org.revcloud.vader.types.validators.ValidatorEtr;

class VaderTest {

  @Test
  void failFastWithFirstFailure() {
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .withValidatorEtrs(
                List.of(
                    bean -> Either.right(NONE),
                    bean -> Either.right(NONE),
                    bean -> Either.left(UNKNOWN_EXCEPTION)))
            .prepare();
    final var result = Vader.validateAndFailFast(new Bean(0), validationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Test
  void errorAccumulation() {
    final List<ValidatorEtr<Bean, ValidationFailure>> validators =
        List.of(
            bean -> Either.right(NONE),
            bean -> Either.left(VALIDATION_FAILURE_1),
            bean -> Either.left(VALIDATION_FAILURE_2));
    final var result =
        Vader.validateAndAccumulateErrors(
            new Bean(0), validators, NONE, ValidationFailure::getValidationFailureForException);
    assertThat(result).containsAll(List.of(NONE, VALIDATION_FAILURE_1, VALIDATION_FAILURE_2));
  }

  @Test
  void errorAccumulationWithSimpleValidators() {
    final List<Validator<Bean, ValidationFailure>> validators =
        List.of(bean -> NONE, bean -> VALIDATION_FAILURE_1, bean -> VALIDATION_FAILURE_2);
    final var result =
        Vader.validateAndAccumulateErrors(
            new Bean(0), validators, NONE, ValidationFailure::getValidationFailureForException);
    assertThat(result).containsAll(List.of(NONE, VALIDATION_FAILURE_1, VALIDATION_FAILURE_2));
  }

  @Test
  void failFastWithFirstFailureForSimpleValidators() {
    // tag::withValidators[]
    final Validator<Bean, ValidationFailure> validator1 = bean -> NONE;
    final Validator<Bean, ValidationFailure> validator2 = bean -> NONE;
    final Validator<Bean, ValidationFailure> validator3 = bean -> UNKNOWN_EXCEPTION;
    final List<Validator<Bean, ValidationFailure>> validatorChain = 
        List.of(validator1, validator2, validator3);
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .withValidators(Tuple.of(validatorChain, NONE))
            .prepare();
    // end::withValidators[]
    final var result = Vader.validateAndFailFast(new Bean(0), validationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Value
  private static class Bean {
    int id;
  }
}
