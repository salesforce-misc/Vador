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
import lombok.Value;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;

class Spec4Test {
	@Test
	void spec4TestWithValidBean() {
		final var config =
				ValidationConfig.<Bean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._4()
												.whenTheseFieldsMatch(
														Map.of(
																Bean::getWhenField1, is(1),
																Bean::getWhenField2, is("2"),
																Bean::getWhenField3, is(new Field(3))))
												.thenThoseFieldsShouldMatch(
														Map.of(
																Bean::getThenField1, is(2),
																Bean::getThenField2, is("3"),
																Bean::getThenField3, is(new Field(4))))
												.orFailWith(INVALID_BEAN))
						.prepare();
		final var validBean = new Bean(1, "2", new Field(3), 2, "3", new Field(4));
		final var result = Vador.validateAndFailFast(validBean, config);
		assertThat(result).isEmpty();
	}

	@Test
	void spec4TestInvalidBeanWithNonMatchingThenFields() {
		final var config =
				ValidationConfig.<Bean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._4()
												.whenTheseFieldsMatch(
														Map.of(
																Bean::getWhenField1, is(1),
																Bean::getWhenField2, is("2"),
																Bean::getWhenField3, is(new Field(3))))
												.thenThoseFieldsShouldMatch(
														Map.of(
																Bean::getThenField1, is(2),
																Bean::getThenField2, is("3"),
																Bean::getThenField3, is(new Field(4))))
												.orFailWith(INVALID_BEAN))
						.prepare();
		final var invalidBean = new Bean(1, "2", new Field(3), 2, "3", new Field(1));
		final var result = Vador.validateAndFailFast(invalidBean, config);
		assertThat(result).contains(INVALID_BEAN);
	}

	@Test
	void spec4TestBeanDoesNotMeetWhenCriteria() {
		final var config =
				ValidationConfig.<Bean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._4()
												.whenTheseFieldsMatch(
														Map.of(
																Bean::getWhenField1, is(1),
																Bean::getWhenField2, is("2"),
																Bean::getWhenField3, is(new Field(3))))
												.thenThoseFieldsShouldMatch(
														Map.of(
																Bean::getThenField1, is(2),
																Bean::getThenField2, is("3"),
																Bean::getThenField3, is(new Field(4))))
												.orFailWith(INVALID_BEAN))
						.prepare();
		final var invalidBean = new Bean(1, "4", new Field(3), 2, "3", new Field(1));
		final var result = Vador.validateAndFailFast(invalidBean, config);
		assertThat(result).isEmpty();
	}

	@Test
	void spec4TestInvalidBeanWithMultiSpec4() {
		final var config =
				ValidationConfig.<Bean, ValidationFailure>toValidate()
						.specify(
								spec ->
										List.of(
												spec._4()
														.whenTheseFieldsMatch(
																Map.of(
																		Bean::getWhenField1, is(1),
																		Bean::getWhenField2, is("2"),
																		Bean::getWhenField3, is(new Field(3))))
														.thenThoseFieldsShouldMatch(
																Map.of(
																		Bean::getThenField1, is(2),
																		Bean::getThenField2, is("3"),
																		Bean::getThenField3, is(new Field(1))))
														.orFailWith(INVALID_BEAN_1),
												spec._4()
														.whenTheseFieldsMatch(
																Map.of(
																		Bean::getWhenField1, is(1),
																		Bean::getWhenField2, is("2"),
																		Bean::getWhenField3, is(new Field(3))))
														.thenThoseFieldsShouldMatch(
																Map.of(
																		Bean::getThenField1, is(2),
																		Bean::getThenField2, is("3"),
																		Bean::getThenField3, is(new Field(4))))
														.orFailWith(INVALID_BEAN_2)))
						.prepare();
		final var invalidBean = new Bean(1, "2", new Field(3), 2, "3", new Field(1));
		final var result = Vador.validateAndFailFast(invalidBean, config);
		assertThat(result).contains(INVALID_BEAN_2);
	}

	@Value
	private static class Bean {
		int whenField1;
		String whenField2;
		Field whenField3;

		int thenField1;
		String thenField2;
		Field thenField3;
	}

	@Value
	private static class Field {
		int id;
	}
}
