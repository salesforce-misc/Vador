package com.salesforce.vador.execution;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sample.consumer.failure.ValidationFailure.NONE;
import static sample.consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;

import com.salesforce.vador.config.ValidationConfig;
import com.salesforce.vador.types.ValidatorAnnotation1;
import io.vavr.Tuple;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;

class VadorAnnotationTest {

	private static final AnnotationBean VALIDATABLE = new AnnotationBean(1, -90, 0);

	private static final BeanMix VALIDATABLE_MIX = new BeanMix(6, "abed");

	private static final BeanCustom VALIDATABLE_CUSTOM = new BeanCustom(new ID("Test Class"));

	private static final BeanCustom2 VALIDATABLE_CUSTOM2 =
			new BeanCustom2(new ID("Test Class1"), new ID("Test Class2"));

	private static final BeanCustom3 VALIDATABLE_CUSTOM3 = new BeanCustom3(new ID("Test Class1"));

	@Test
	void failFastWithFirstFailureWithValidatorAnnotation() {
		final var validationConfig =
				ValidationConfig.<AnnotationBean, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		final var result = Vador.validateAndFailFast(VALIDATABLE, validationConfig);
		assertThat(result).isEmpty();
	}

	@Test
	void failFastWithFirstFailureWithValidatorAnnotationError() {
		final var validationConfig =
				ValidationConfig.<AnnotationBean, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		final var result = Vador.validateAndFailFast(new AnnotationBean(-9, -9, 1), validationConfig);
		assertThat(result).contains(UNKNOWN_EXCEPTION);
	}

	@Test
	void failFastWithFirstFailureWithValidatorAnnotationRunTimeError() {
		final var validationConfig =
				ValidationConfig.<BeanMix, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		assertThrows(
				ClassCastException.class,
				() -> {
					Vador.validateAndFailFast(VALIDATABLE_MIX, validationConfig);
				});
	}

	@Test
	void failFastWithFirstFailureWithValidatorCustomAnnotation() {
		final var validationConfig =
				ValidationConfig.<BeanCustom, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		final var result = Vador.validateAndFailFast(VALIDATABLE_CUSTOM, validationConfig);
		assertThat(result).isEmpty();
	}

	@Test
	void failFastWithFirstFailureWithValidatorCustomAnnotationWithError() {
		final var validationConfig =
				ValidationConfig.<BeanCustom2, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		final var result = Vador.validateAndFailFast(VALIDATABLE_CUSTOM2, validationConfig);
		assertThat(result).contains(UNKNOWN_EXCEPTION);
	}

	@Test
	void failFastWithFirstFailureWithValidatorCustomAnnotationWithDifferentInterface() {
		final var validationConfig =
				ValidationConfig.<BeanCustom3, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		assertThrows(
				IllegalArgumentException.class,
				() -> {
					Vador.validateAndFailFast(VALIDATABLE_CUSTOM3, validationConfig);
				});
	}

	@Test
	void failFastWithFirstFailureWithValidatorAnnotationMaxMin() {
		final var validationConfig =
				ValidationConfig.<BeanInt, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		final var result = Vador.validateAndFailFast(new BeanInt(99, 1000), validationConfig);
		assertThat(result).isEmpty();
	}

	@Test
	void failFastWithFirstFailureWithValidatorAnnotationMaxMinError() {
		final var validationConfig =
				ValidationConfig.<BeanInt, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		final var result = Vador.validateAndFailFast(new BeanInt(-9, -9), validationConfig);
		assertThat(result).contains(UNKNOWN_EXCEPTION);
	}

	@Test
	void failFastWithFirstFailureWithValidatorAnnotationNotDefinedByVador() {
		final var validationConfig =
				ValidationConfig.<BeanFailure, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		final var result = Vador.validateAndFailFast(new BeanFailure(-9), validationConfig);
		assertThat(result).isEmpty();
	}

	@Test
	void failFastWithFirstFailureWithValidatorAnnotationRequiredInt() {
		final var validationConfig =
				ValidationConfig.<BeanRequired, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		final var result = Vador.validateAndFailFast(new BeanRequired(1), validationConfig);
		assertThat(result).isEmpty();
	}

	@Test
	void failFastWithFirstFailureWithValidatorAnnotationRequiredString() {
		final var validationConfig =
				ValidationConfig.<BeanRequired, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		final var result = Vador.validateAndFailFast(new BeanRequired("Test"), validationConfig);
		assertThat(result).isEmpty();
	}

	@Test
	void failFastWithFirstFailureWithValidatorAnnotationRequiredNull() {
		final var validationConfig =
				ValidationConfig.<BeanRequired, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		final var result = Vador.validateAndFailFast(new BeanRequired(null), validationConfig);
		assertThat(result).contains(UNKNOWN_EXCEPTION);
	}

	@Test
	void failFastWithFirstFailureWithValidatorAnnotationRequiredEmptyString() {
		final var validationConfig =
				ValidationConfig.<BeanRequired, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		final var result = Vador.validateAndFailFast(new BeanRequired(""), validationConfig);
		assertThat(result).contains(UNKNOWN_EXCEPTION);
	}

	@Test
	void failFastWithFirstFailureWithValidatorAnnotationRequiredEmptyList() {
		final var validationConfig =
				ValidationConfig.<BeanRequired, ValidationFailure>toValidate()
						.forAnnotations(Tuple.of(Map.of("unexpectedException", UNKNOWN_EXCEPTION), NONE))
						.prepare();
		final var result = Vador.validateAndFailFast(new BeanRequired(emptyList()), validationConfig);
		assertThat(result).contains(UNKNOWN_EXCEPTION);
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
