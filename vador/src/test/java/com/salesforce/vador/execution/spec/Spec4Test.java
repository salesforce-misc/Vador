/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.execution.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static sample.consumer.failure.ValidationFailure.INVALID_BEAN;
import static sample.consumer.failure.ValidationFailure.INVALID_BEAN_1;
import static sample.consumer.failure.ValidationFailure.INVALID_BEAN_2;

import com.salesforce.vador.config.ValidationConfig;
import com.salesforce.vador.execution.Vador;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;

class Spec4Test {
	@Test
	void spec4TestWithValidBean() {
		final var config =
				ValidationConfig.<Spec4Bean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._4()
												.whenTheseFieldsMatch(
														Map.of(
																Spec4Bean::getWhenField1, is(1),
																Spec4Bean::getWhenField2, is("2"),
																Spec4Bean::getWhenField3, is(new Spec4Field(3))))
												.thenThoseFieldsShouldMatch(
														Map.of(
																Spec4Bean::getThenField1, is(2),
																Spec4Bean::getThenField2, is("3"),
																Spec4Bean::getThenField3, is(new Spec4Field(4))))
												.orFailWith(INVALID_BEAN))
						.prepare();
		final var validBean = new Spec4Bean(1, "2", new Spec4Field(3), 2, "3", new Spec4Field(4));
		final var result = Vador.validateAndFailFast(validBean, config);
		assertThat(result).isEmpty();
	}

	@Test
	void spec4TestInvalidBeanWithNonMatchingThenFields() {
		final var config =
				ValidationConfig.<Spec4Bean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._4()
												.whenTheseFieldsMatch(
														Map.of(
																Spec4Bean::getWhenField1, is(1),
																Spec4Bean::getWhenField2, is("2"),
																Spec4Bean::getWhenField3, is(new Spec4Field(3))))
												.thenThoseFieldsShouldMatch(
														Map.of(
																Spec4Bean::getThenField1, is(2),
																Spec4Bean::getThenField2, is("3"),
																Spec4Bean::getThenField3, is(new Spec4Field(4))))
												.orFailWith(INVALID_BEAN))
						.prepare();
		final var invalidBean = new Spec4Bean(1, "2", new Spec4Field(3), 2, "3", new Spec4Field(1));
		final var result = Vador.validateAndFailFast(invalidBean, config);
		assertThat(result).contains(INVALID_BEAN);
	}

	@Test
	void spec4TestBeanDoesNotMeetWhenCriteria() {
		final var config =
				ValidationConfig.<Spec4Bean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._4()
												.whenTheseFieldsMatch(
														Map.of(
																Spec4Bean::getWhenField1, is(1),
																Spec4Bean::getWhenField2, is("2"),
																Spec4Bean::getWhenField3, is(new Spec4Field(3))))
												.thenThoseFieldsShouldMatch(
														Map.of(
																Spec4Bean::getThenField1, is(2),
																Spec4Bean::getThenField2, is("3"),
																Spec4Bean::getThenField3, is(new Spec4Field(4))))
												.orFailWith(INVALID_BEAN))
						.prepare();
		final var invalidBean = new Spec4Bean(1, "4", new Spec4Field(3), 2, "3", new Spec4Field(1));
		final var result = Vador.validateAndFailFast(invalidBean, config);
		assertThat(result).isEmpty();
	}

	@Test
	void spec4TestInvalidBeanWithMultiSpec4() {
		final var config =
				ValidationConfig.<Spec4Bean, ValidationFailure>toValidate()
						.specify(
								spec ->
										List.of(
												spec._4()
														.whenTheseFieldsMatch(
																Map.of(
																		Spec4Bean::getWhenField1, is(1),
																		Spec4Bean::getWhenField2, is("2"),
																		Spec4Bean::getWhenField3, is(new Spec4Field(3))))
														.thenThoseFieldsShouldMatch(
																Map.of(
																		Spec4Bean::getThenField1, is(2),
																		Spec4Bean::getThenField2, is("3"),
																		Spec4Bean::getThenField3, is(new Spec4Field(1))))
														.orFailWith(INVALID_BEAN_1),
												spec._4()
														.whenTheseFieldsMatch(
																Map.of(
																		Spec4Bean::getWhenField1, is(1),
																		Spec4Bean::getWhenField2, is("2"),
																		Spec4Bean::getWhenField3, is(new Spec4Field(3))))
														.thenThoseFieldsShouldMatch(
																Map.of(
																		Spec4Bean::getThenField1, is(2),
																		Spec4Bean::getThenField2, is("3"),
																		Spec4Bean::getThenField3, is(new Spec4Field(4))))
														.orFailWith(INVALID_BEAN_2)))
						.prepare();
		final var invalidBean = new Spec4Bean(1, "2", new Spec4Field(3), 2, "3", new Spec4Field(1));
		final var result = Vador.validateAndFailFast(invalidBean, config);
		assertThat(result).contains(INVALID_BEAN_2);
	}
}
