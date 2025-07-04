/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.execution.spec;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.salesforce.vador.config.ValidationConfig;
import com.salesforce.vador.matchers.DateMatchers;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import lombok.Value;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;

class Spec3Test {

	@Test
	void testDates() {
		final var specName = "CompareDates";
		final var validationConfig =
				ValidationConfig.<DatesBean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._3()
												.nameForTest(specName)
												.when(DatesBean::isCompareDates)
												.matches(is(true))
												.thenField1(DatesBean::getDate1)
												.thenField2(DatesBean::getDate2)
												.shouldRelateWithFn(DateMatchers.isBeforeIfBothArePresent()))
						.prepare();

		final var validBean =
				new DatesBean(
						true,
						new GregorianCalendar(2021, Calendar.APRIL, 27).getTime(),
						new GregorianCalendar(2021, Calendar.APRIL, 28).getTime());
		assertTrue(
				validationConfig
						.getPredicateOfSpecForTest(specName)
						.map(spec -> spec.test(validBean))
						.orElse(false));

		final var invalidBean =
				new DatesBean(
						true,
						new GregorianCalendar(2021, Calendar.APRIL, 29).getTime(),
						new GregorianCalendar(2021, Calendar.APRIL, 28).getTime());
		assertFalse(
				validationConfig
						.getPredicateOfSpecForTest(specName)
						.map(spec -> spec.test(invalidBean))
						.orElse(true));
	}

	@Test
	void testDatesWithNulls() {
		final var specName = "CompareDates";
		final var validationConfig =
				ValidationConfig.<DatesBean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._3()
												.nameForTest(specName)
												.when(DatesBean::isCompareDates)
												.matches(is(true))
												.thenField1(DatesBean::getDate1)
												.thenField2(DatesBean::getDate2)
												.shouldRelateWithFn(DateMatchers.isBeforeIfBothArePresent()))
						.prepare();
		final var invalidBean1 =
				new DatesBean(true, null, new GregorianCalendar(2021, Calendar.APRIL, 29).getTime());
		assertFalse(
				validationConfig
						.getPredicateOfSpecForTest(specName)
						.map(spec -> spec.test(invalidBean1))
						.orElse(true));

		final var invalidBean2 =
				new DatesBean(true, new GregorianCalendar(2021, Calendar.APRIL, 29).getTime(), null);
		assertFalse(
				validationConfig
						.getPredicateOfSpecForTest(specName)
						.map(spec -> spec.test(invalidBean2))
						.orElse(true));
	}

	@Test
	void spec3WithFieldComparison() {
		final var specName = "CompareFields";
		final var validationConfig =
				ValidationConfig.<Bean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._3()
												.nameForTest(specName)
												.when(Bean::isCompareFields)
												.matches(is(true))
												.thenField1(Bean::getBdom)
												.thenField2(Bean::getStartDate)
												.shouldRelateWithFn(DateMatchers.isEqualToDayOfDate())
												.orField1ShouldMatch(nullValue()))
						.prepare();
		final var invalidBean =
				new Bean(true, 2, new GregorianCalendar(2021, Calendar.APRIL, 1).getTime());
		assertFalse(
				validationConfig
						.getPredicateOfSpecForTest(specName)
						.map(spec -> spec.test(invalidBean))
						.orElse(true));

		final var validBean1 =
				new Bean(true, null, new GregorianCalendar(2021, Calendar.APRIL, 1).getTime());
		assertTrue(
				validationConfig
						.getPredicateOfSpecForTest(specName)
						.map(spec -> spec.test(validBean1))
						.orElse(false));

		final var validBean2 =
				new Bean(true, 1, new GregorianCalendar(2021, Calendar.APRIL, 1).getTime());
		assertTrue(
				validationConfig
						.getPredicateOfSpecForTest(specName)
						.map(spec -> spec.test(validBean2))
						.orElse(false));
	}

	@Value
	private static class DatesBean {

		boolean compareDates;
		Date date1;
		Date date2;
	}

	@Value
	private static class Bean {

		boolean compareFields;
		Integer bdom;
		Date startDate;
	}
}
