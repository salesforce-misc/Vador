package com.salesforce.vador.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sample.consumer.failure.ValidationFailure.NONE;
import static sample.consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;

import com.salesforce.vador.annotation.Negative;
import com.salesforce.vador.annotation.Positive;
import com.salesforce.vador.annotation.ValidateWith;
import com.salesforce.vador.config.ValidationConfig;
import io.vavr.Tuple;
import java.util.Map;
import lombok.Value;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;

public class VadorAnnotationTest {

  private static final Bean VALIDATABLE = new Bean(6, -90);

  private static final BeanMix VALIDATABLEMIX = new BeanMix(6, "abed");

  @Test
  void failFastWithFirstFailureWithValidatorAnnotation() {
    final var validationConfig =
        ValidationConfig.<VadorAnnotationTest.Bean, ValidationFailure>toValidate()
            .forAnnotation(Tuple.of(Map.of("Unexpected_exception", UNKNOWN_EXCEPTION), NONE))
            .prepare();
    final var result = Vador.validateAndFailFast(VALIDATABLE, validationConfig);
    assertThat(result).isEmpty();
  }

  @Test
  void failFastWithFirstFailureWithValidatorAnnotationError() {
    final var validationConfig =
        ValidationConfig.<VadorAnnotationTest.Bean, ValidationFailure>toValidate()
            .forAnnotation(Tuple.of(Map.of("Unexpected_exception", UNKNOWN_EXCEPTION), NONE))
            .prepare();
    final var result = Vador.validateAndFailFast(new Bean(-9, -9), validationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Test
  void failFastWithFirstFailureWithValidatorAnnotationRunTimeError() {
    final var validationConfig =
        ValidationConfig.<VadorAnnotationTest.BeanMix, ValidationFailure>toValidate()
            .forAnnotation(Tuple.of(Map.of("Unexpected_exception", UNKNOWN_EXCEPTION), NONE))
            .prepare();
    assertThrows(
        RuntimeException.class,
        () -> {
          Vador.validateAndFailFast(VALIDATABLEMIX, validationConfig);
        });
  }

  @Value
  private static class Bean {
    @ValidateWith(validator = Positive.class, failureKey = "Unexpected_exception")
    int idOne;

    @ValidateWith(validator = Negative.class, failureKey = "Unexpected_exception")
    int idTwo;
  }

  @Value
  private static class BeanMix {
    @ValidateWith(validator = Positive.class, failureKey = "Unexpected_exception")
    int idOne;

    @ValidateWith(validator = Negative.class, failureKey = "Unexpected_exception")
    String idTwo;
  }
}
