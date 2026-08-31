/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.execution.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static sample.consumer.failure.ValidationFailure.INVALID_BEAN;
import static sample.consumer.failure.ValidationFailure.INVALID_BEAN_1;
import static sample.consumer.failure.ValidationFailure.INVALID_BEAN_2;

import com.salesforce.vador.config.ValidationConfig;
import com.salesforce.vador.execution.Vador;
import io.vavr.Tuple;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;

class Spec5Test {
	@Test
	void spec5TestWithValidBean() {
		final var config =
				ValidationConfig.<Spec5Bean1, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._5()
												.whenAllTheseFieldsMatch(
														Tuple.of(
																List.of(
																		Spec5Bean1::getWhenField1,
																		Spec5Bean1::getWhenField2,
																		Spec5Bean1::getWhenField3),
																notNullValue()))
												.thenAllThoseFieldsShouldMatch(
														Tuple.of(
																List.of(
																		Spec5Bean1::getThenField1,
																		Spec5Bean1::getThenField2,
																		Spec5Bean1::getThenField3),
																nullValue()))
												.orFailWith(INVALID_BEAN))
						.prepare();

		final var validBean = new Spec5Bean1(1, "2", new Spec5Field(3), null, null, null);
		final var result = Vador.validateAndFailFast(validBean, config);
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("One Then field doesn't match")
	void spec5TestWithInValidBean() {
		final var config =
				ValidationConfig.<Spec5Bean1, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._5()
												.whenAllTheseFieldsMatch(
														Tuple.of(
																List.of(
																		Spec5Bean1::getWhenField1,
																		Spec5Bean1::getWhenField2,
																		Spec5Bean1::getWhenField3),
																notNullValue()))
												.thenAllThoseFieldsShouldMatch(
														Tuple.of(
																List.of(
																		Spec5Bean1::getThenField1,
																		Spec5Bean1::getThenField2,
																		Spec5Bean1::getThenField3),
																nullValue()))
												.orFailWith(INVALID_BEAN))
						.prepare();

		final var validBean = new Spec5Bean1(1, "2", new Spec5Field(3), null, "doesn't match", null);
		final var result = Vador.validateAndFailFast(validBean, config);
		assertThat(result).contains(INVALID_BEAN);
	}

	@Test
	@DisplayName("One When field doesn't match criteria")
	void spec5TestBeanDoesNotMeetWhenCriteria() {
		final var config =
				ValidationConfig.<Spec5Bean1, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._5()
												.whenAllTheseFieldsMatch(
														Tuple.of(
																List.of(
																		Spec5Bean1::getWhenField1,
																		Spec5Bean1::getWhenField2,
																		Spec5Bean1::getWhenField3),
																notNullValue()))
												.thenAllThoseFieldsShouldMatch(
														Tuple.of(
																List.of(
																		Spec5Bean1::getThenField1,
																		Spec5Bean1::getThenField2,
																		Spec5Bean1::getThenField3),
																nullValue()))
												.orFailWith(INVALID_BEAN))
						.prepare();

		final var validBean = new Spec5Bean1(1, null, new Spec5Field(3), null, "doesn't match", null);
		final var result = Vador.validateAndFailFast(validBean, config);
		assertThat(result).isEmpty();
	}

	@Test
	void spec5TestInvalidBeanWithMultiSpec5() {
		final var config =
				ValidationConfig.<Spec5Bean2, ValidationFailure>toValidate()
						.specify(
								spec ->
										List.of(
												spec._5()
														.whenAllTheseFieldsMatch(
																Tuple.of(
																		List.of(Spec5Bean2::getWhenField1, Spec5Bean2::getWhenField2),
																		equalTo(1)))
														.thenAllThoseFieldsShouldMatch(
																Tuple.of(
																		List.of(Spec5Bean2::getThenField1, Spec5Bean2::getThenField2),
																		equalTo("1")))
														.orFailWith(INVALID_BEAN_1),
												spec._5()
														.whenAllTheseFieldsMatch(
																Tuple.of(
																		List.of(Spec5Bean2::getWhenField1, Spec5Bean2::getWhenField2),
																		equalTo(1)))
														.thenAllThoseFieldsShouldMatch(
																Tuple.of(
																		List.of(Spec5Bean2::getThenField1, Spec5Bean2::getThenField2),
																		nullValue()))
														.orFailWith(INVALID_BEAN_2)))
						.prepare();
		final var invalidBean = new Spec5Bean2(1, 1, "1", "1");
		final var result = Vador.validateAndFailFast(invalidBean, config);
		assertThat(result).contains(INVALID_BEAN_2);
	}
}
