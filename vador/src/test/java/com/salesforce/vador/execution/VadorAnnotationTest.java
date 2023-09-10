package com.salesforce.vador.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sample.consumer.failure.ValidationFailure.NONE;
import static sample.consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;

import com.salesforce.vador.annotation.MaxForInt;
import com.salesforce.vador.annotation.MinForInt;
import com.salesforce.vador.annotation.Negative;
import com.salesforce.vador.annotation.NonNegative;
import com.salesforce.vador.annotation.Positive;
import com.salesforce.vador.annotation.ValidateWith;
import com.salesforce.vador.config.ValidationConfig;
import com.salesforce.vador.types.ValidatorAnnotation1;
import io.vavr.Tuple;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;

public class VadorAnnotationTest {

  private static final Bean VALIDATABLE = new Bean(1, -90, 0);

  private static final BeanMix VALIDATABLEMIX = new BeanMix(6, "abed");

  private static final BeanCustom VALIDATBLECUSTOM = new BeanCustom(new ID("Test Class"));

  private static final BeanCustom2 VALIDATBLECUSTOM2 =
      new BeanCustom2(new ID("Test Class1"), new ID("Test Class2"));

  private static final BeanCustom3 VALIDATBLECUSTOM3 = new BeanCustom3(new ID("Test Class1"));

  private static final BeanInt VALIDATABLEINT = new BeanInt(99, 1000);

  @Test
  void failFastWithFirstFailureWithValidatorAnnotation() {
    final var validationConfig =
        ValidationConfig.<VadorAnnotationTest.Bean, ValidationFailure>toValidate()
            .forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
            .prepare();
    final var result = Vador.validateAndFailFast(VALIDATABLE, validationConfig);
    assertThat(result).isEmpty();
  }

  @Test
  void failFastWithFirstFailureWithValidatorAnnotationError() {
    final var validationConfig =
        ValidationConfig.<VadorAnnotationTest.Bean, ValidationFailure>toValidate()
            .forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
            .prepare();
    final var result = Vador.validateAndFailFast(new Bean(-9, -9, 1), validationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Test
  void failFastWithFirstFailureWithValidatorAnnotationRunTimeError() {
    final var validationConfig =
        ValidationConfig.<VadorAnnotationTest.BeanMix, ValidationFailure>toValidate()
            .forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
            .prepare();
    assertThrows(
        ClassCastException.class,
        () -> {
          Vador.validateAndFailFast(VALIDATABLEMIX, validationConfig);
        });
  }

  @Test
  void failFastWithFirstFailureWithValidatorCustomAnnotation() {
    final var validationConfig =
        ValidationConfig.<VadorAnnotationTest.BeanCustom, ValidationFailure>toValidate()
            .forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
            .prepare();
    final var result = Vador.validateAndFailFast(VALIDATBLECUSTOM, validationConfig);
    assertThat(result).isEmpty();
  }

  @Test
  void failFastWithFirstFailureWithValidatorCustomAnnotationWithError() {
    final var validationConfig =
        ValidationConfig.<VadorAnnotationTest.BeanCustom2, ValidationFailure>toValidate()
            .forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
            .prepare();
    final var result = Vador.validateAndFailFast(VALIDATBLECUSTOM2, validationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Test
  void failFastWithFirstFailureWithValidatorCustomAnnotationWithDifferentInterface() {
    final var validationConfig =
        ValidationConfig.<VadorAnnotationTest.BeanCustom3, ValidationFailure>toValidate()
            .forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
            .prepare();
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          Vador.validateAndFailFast(VALIDATBLECUSTOM3, validationConfig);
        });
  }

  @Test
  void failFastWithFirstFailureWithValidatorAnnotationMaxMin() {
    final var validationConfig =
        ValidationConfig.<VadorAnnotationTest.BeanInt, ValidationFailure>toValidate()
            .forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
            .prepare();
    final var result = Vador.validateAndFailFast(VALIDATABLEINT, validationConfig);
    assertThat(result).isEmpty();
  }

  @Test
  void failFastWithFirstFailureWithValidatorAnnotationMaxMinError() {
    final var validationConfig =
        ValidationConfig.<VadorAnnotationTest.BeanInt, ValidationFailure>toValidate()
            .forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
            .prepare();
    final var result = Vador.validateAndFailFast(new BeanInt(-9, -9), validationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Value
  private static class Bean {
    @Positive(failureKey = "unexpectedException")
    int idOne;

    @Negative(failureKey = "unexpectedException")
    int idTwo;

    @NonNegative(failureKey = "unexpectedException")
    int idThree;
  }

  @Value
  private static class BeanMix {
    @Positive(failureKey = "unexpectedException")
    int idOne;

    @Negative(validator = Negative.class, failureKey = "unexpectedException")
    String idTwo;
  }

  @Value
  private static class BeanCustom {
    @ValidateWith(validator = myIdValidator1.class, failureKey = "unexpectedException")
    ID idOne;
  }

  @Value
  private static class BeanCustom2 {
    @ValidateWith(validator = myIdValidator1.class, failureKey = "unexpectedException")
    ID idOne;

    @ValidateWith(validator = myIdValidator2.class, failureKey = "unexpectedException")
    ID idTwo;
  }

  @Value
  private static class BeanCustom3 {
    @ValidateWith(validator = myIdValidator3.class, failureKey = "unexpectedException")
    ID idOne;
  }

  
  @Value
  private static class BeanInt {
    @MaxForInt(limit = 100, failureKey = "unexpectedException")
    int idOne;

    @MinForInt(limit = 500, failureKey = "unexpectedException")
    int idTwo;
  }

  static class ID {
    String value;

    ID(String value) {
      this.value = value;
    }
  }

  public static class myIdValidator1 implements ValidatorAnnotation1<ID, ValidationFailure> {
    @Override
    public ValidationFailure validate(
        @NotNull Field field, ID bean, ValidationFailure failure, ValidationFailure none) {
      if (bean instanceof ID) {
        return NONE;
      } else {
        return UNKNOWN_EXCEPTION;
      }
    }
  }

  public static class myIdValidator2 implements ValidatorAnnotation1<ID, ValidationFailure> {
    @Override
    public ValidationFailure validate(
        @NotNull Field field, ID bean, ValidationFailure failure, ValidationFailure none) {
      if (Objects.equals(bean.value, "Test")) {
        return NONE;
      } else {
        return UNKNOWN_EXCEPTION;
      }
    }
  }

  public static class myIdValidator3 {
    public ValidationFailure validate(
        @NotNull Field field, ID bean, ValidationFailure failure, ValidationFailure none) {
      if (Objects.equals(bean.value, "Test")) {
        return NONE;
      } else {
        return UNKNOWN_EXCEPTION;
      }
    }
  }
}
