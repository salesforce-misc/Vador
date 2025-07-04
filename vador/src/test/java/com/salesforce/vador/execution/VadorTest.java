/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static sample.consumer.failure.ValidationFailure.NONE;
import static sample.consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static sample.consumer.failure.ValidationFailure.OUT_OF_BOUND;
import static sample.consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;
import static sample.consumer.failure.ValidationFailure.VALIDATION_FAILURE_1;
import static sample.consumer.failure.ValidationFailure.VALIDATION_FAILURE_2;

import com.salesforce.vador.config.ValidationConfig;
import com.salesforce.vador.types.Validator;
import com.salesforce.vador.types.ValidatorEtr;
import io.vavr.Tuple;
import io.vavr.control.Either;
import java.util.Collections;
import java.util.List;
import lombok.Value;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;
import sample.consumer.failure.ValidationFailureMessage;

class VadorTest {

	private static final Bean VALIDATABLE = new Bean(0);

	// tag::failFastDemo[]
	@Test
	void failFastWithFirstFailureWithValidator() {
		// tag::withValidators[]
		final List<Validator<Bean, ValidationFailure>> validatorChain =
				List.of(validator(1, 9), validator(3, 7), validator(5, 5));
		final var validationConfig =
				ValidationConfig.<Bean, ValidationFailure>toValidate()
						.withValidators(Tuple.of(validatorChain, NONE))
						.prepare();
		// end::withValidators[]
		final var result = Vador.validateAndFailFast(new Bean(0), validationConfig);
		assertThat(result).contains(OUT_OF_BOUND);
	}

	/**
	 * You can pass any number of arguments (like lowerLimit, upperLimit), to write your validator
	 * closure
	 */
	Validator<Bean, ValidationFailure> validator(int lowerLimit, int upperLimit) {
		return bean -> bean.value >= lowerLimit && bean.value <= upperLimit ? NONE : OUT_OF_BOUND;
	}

	// end::failFastDemo[]

	@Test
	void noFailure() {
		// tag::withValidators[]
		final Validator<Bean, ValidationFailure> validator1 = bean -> NONE;
		final Validator<Bean, ValidationFailure> validator2 = bean -> NONE;
		final List<Validator<Bean, ValidationFailure>> validatorChain = List.of(validator1, validator2);
		final var validationConfig =
				ValidationConfig.<Bean, ValidationFailure>toValidate()
						.withValidators(Tuple.of(validatorChain, NONE))
						.prepare();
		// end::withValidators[]
		final var result = Vador.validateAndFailFast(VALIDATABLE, validationConfig);
		assertThat(result).isEmpty();
	}

	@Test
	void failFastRecursively() {
		final Validator<RecursiveBean, ValidationFailure> validator =
				recursiveBean -> recursiveBean.id == -1 ? UNKNOWN_EXCEPTION : NONE;
		final var recursiveBean =
				new RecursiveBean(
						1,
						List.of(
								new RecursiveBean(11, Collections.emptyList()),
								new RecursiveBean(-1, Collections.emptyList()),
								new RecursiveBean(13, Collections.emptyList())));
		final var validationConfig =
				ValidationConfig.<RecursiveBean, ValidationFailure>toValidate()
						.withValidator(validator, NONE)
						.withRecursiveMapper(RecursiveBean::getRecursiveBeans)
						.prepare();
		final var result = Vador.validateAndFailFast(recursiveBean, validationConfig);
		assertThat(result).contains(UNKNOWN_EXCEPTION);
	}

	@Test
	void failFastWithFirstFailureWithValidatorEtr() {
		final ValidatorEtr<Bean, ValidationFailure> validator1 = bean -> Either.right(NONE);
		final ValidatorEtr<Bean, ValidationFailure> validator2 = bean -> Either.right(NONE);
		final ValidatorEtr<Bean, ValidationFailure> validator3 = bean -> Either.left(UNKNOWN_EXCEPTION);
		final List<ValidatorEtr<Bean, ValidationFailure>> validatorChain =
				List.of(validator1, validator2, validator3);
		final var validationConfig =
				ValidationConfig.<Bean, ValidationFailure>toValidate()
						.withValidatorEtrs(validatorChain)
						.prepare();
		final var result = Vador.validateAndFailFast(VALIDATABLE, validationConfig);
		assertThat(result).contains(UNKNOWN_EXCEPTION);
	}

	@Test
	void errorAccumulationWithValidators() {
		final List<Validator<Bean, ValidationFailure>> validators =
				List.of(bean -> NONE, bean -> VALIDATION_FAILURE_1, bean -> VALIDATION_FAILURE_2);
		final var result =
				Vador.validateAndAccumulateErrors(
						VALIDATABLE, validators, NONE, ValidationFailure::getValidationFailureForException);
		assertThat(result).containsAll(List.of(NONE, VALIDATION_FAILURE_1, VALIDATION_FAILURE_2));
	}

	@Test
	void errorAccumulationWithValidatorEtrs() {
		final List<ValidatorEtr<Bean, ValidationFailure>> validatorEtrs =
				List.of(
						bean -> Either.right(NONE),
						bean -> Either.left(VALIDATION_FAILURE_1),
						bean -> Either.left(VALIDATION_FAILURE_2));
		final var result =
				Vador.validateAndAccumulateErrors(
						VALIDATABLE, validatorEtrs, NONE, ValidationFailure::getValidationFailureForException);
		assertThat(result).containsAll(List.of(NONE, VALIDATION_FAILURE_1, VALIDATION_FAILURE_2));
	}

	@Test
	void throwableMapperTest() {
		final var expMsg = "expMsg";
		final var validationConfig =
				ValidationConfig.<Bean, ValidationFailure>toValidate()
						.withValidator(
								ignore -> {
									throw new RuntimeException(expMsg);
								},
								NOTHING_TO_VALIDATE)
						.prepare();
		final var result =
				Vador.validateAndFailFast(
						new Bean(0), validationConfig, ValidationFailure::getValidationFailureForException);

		assertThat(result).isPresent();
		assertThat(result.get().getValidationFailureMessage())
				.isEqualTo(ValidationFailureMessage.UNKNOWN_EXCEPTION);
		assertThat(result.get().getExceptionMsg()).isEqualTo(expMsg);
	}

	@Value
	private static class Bean {
		int value;
	}

	@Value
	private static class RecursiveBean {
		int id;
		List<RecursiveBean> recursiveBeans;
	}
}
