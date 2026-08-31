/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.execution;

import static com.salesforce.vador.execution.strategies.util.Utils.findAndFilterInvalids;
import static com.salesforce.vador.execution.strategies.util.Utils.findFirstInvalid;
import static sample.consumer.failure.ValidationFailure.DUPLICATE_ITEM;
import static sample.consumer.failure.ValidationFailure.DUPLICATE_ITEM_1;
import static sample.consumer.failure.ValidationFailure.DUPLICATE_ITEM_2;
import static sample.consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static sample.consumer.failure.ValidationFailure.NULL_KEY;

import com.salesforce.vador.config.BatchValidationConfig;
import com.salesforce.vador.config.FilterDuplicatesConfig;
import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;

class UtilsTest {

	// tag::batch-bean-demo[]
	@DisplayName("FailForDuplicates configured. FAIL: Null Validatables, FAIL: Duplicates")
	@Test
	void filterNullValidatablesAndFailDuplicates() {
		final List<UtilsBean> nullValidatables = io.vavr.collection.List.of(null, null);
		final var duplicateValidatables =
				List.of(
						new UtilsBean("802xx000001ni4xAAA"),
						new UtilsBean("802xx000001ni4xAAA"),
						new UtilsBean("802xx000001ni4xAAA"));
		final var validatables =
				nullValidatables
						.appendAll(duplicateValidatables)
						.appendAll(List.of(new UtilsBean("1"), new UtilsBean("2"), new UtilsBean("3")));

		final var batchValidationConfig =
				BatchValidationConfig.<UtilsBean, ValidationFailure>toValidate()
						.findAndFilterDuplicatesConfig(
								FilterDuplicatesConfig.<UtilsBean, ValidationFailure>toValidate()
										.findAndFilterDuplicatesWith(UtilsBean::getId)
										.andFailDuplicatesWith(DUPLICATE_ITEM))
						.prepare();
		final var results =
				io.vavr.collection.List.ofAll(
						findAndFilterInvalids(
								validatables.toJavaList(),
								NOTHING_TO_VALIDATE,
								batchValidationConfig.getFindAndFilterDuplicatesConfigs()));

		final var skippedNullValidatables = results.take(2);
		Assertions.assertThat(skippedNullValidatables)
				.allMatch(Either::isRight)
				.allMatch(r -> r.get() == null);
		final var failedDuplicates = results.drop(2).take(3);
		Assertions.assertThat(failedDuplicates)
				.allMatch(Either::isLeft)
				.allMatch(r -> r.getLeft() == DUPLICATE_ITEM);

		final var valids = results.drop(5);
		org.junit.jupiter.api.Assertions.assertTrue(valids.forAll(Either::isRight));
		valids.forEachWithIndex(
				(r, i) ->
						org.junit.jupiter.api.Assertions.assertEquals(String.valueOf(i + 1), r.get().getId()));
	}

	// end::batch-bean-demo[]

	// tag::batch-bean-multikey-demo[]
	@DisplayName(
			"Multiple Filters - FailForDuplicates configured. FAIL: NullValidatbles, FAIL: Duplicates")
	@Test
	void filterNullValidatablesAndFailDuplicatesForMultipleFilters() {
		final List<UtilsMultiKeyBean> nullValidatables = List.of(null, null);
		final var duplicateValidatables =
				List.of(
						new UtilsMultiKeyBean("802xx000001ni4xAAA", "802xx000001ni5xAAA"),
						new UtilsMultiKeyBean("802xx000001ni4xAAA", "802xx000001ni4xAAA"),
						new UtilsMultiKeyBean("802xx000001ni5x", "802xx000001ni4xAAA"));
		final var validatables =
				nullValidatables
						.appendAll(duplicateValidatables)
						.appendAll(
								List.of(
										new UtilsMultiKeyBean("1", "1"),
										new UtilsMultiKeyBean("2", "2"),
										new UtilsMultiKeyBean("3", "3")));

		final Function1<UtilsMultiKeyBean, Object> id1Mapper =
				container -> container.getId1() == null ? null : container.getId1();
		final Function1<UtilsMultiKeyBean, Object> id2Mapper =
				container -> container.getId2() == null ? null : container.getId2();
		final var batchValidationConfig =
				BatchValidationConfig.<UtilsMultiKeyBean, ValidationFailure>toValidate()
						.findAndFilterDuplicatesConfigs(
								java.util.List.of(
										FilterDuplicatesConfig.<UtilsMultiKeyBean, ValidationFailure>toValidate()
												.findAndFilterDuplicatesWith(id1Mapper)
												.andFailDuplicatesWith(DUPLICATE_ITEM_1),
										FilterDuplicatesConfig.<UtilsMultiKeyBean, ValidationFailure>toValidate()
												.findAndFilterDuplicatesWith(id2Mapper)
												.andFailDuplicatesWith(DUPLICATE_ITEM_2)))
						.prepare();
		final var results =
				List.ofAll(
						findAndFilterInvalids(
								validatables.toJavaList(),
								NOTHING_TO_VALIDATE,
								batchValidationConfig.getFindAndFilterDuplicatesConfigs()));

		final var skippedNullValidatables = results.take(2);
		Assertions.assertThat(skippedNullValidatables)
				.allMatch(Either::isRight)
				.allMatch(r -> r.get() == null);
		final var failedDuplicates1 = results.drop(2).take(2);
		Assertions.assertThat(failedDuplicates1).allMatch(r -> r.getLeft() == DUPLICATE_ITEM_1);

		final var failedDuplicates2 = results.drop(4).take(1);
		Assertions.assertThat(failedDuplicates2).allMatch(r -> r.getLeft() == DUPLICATE_ITEM_2);

		final var valids = results.drop(5);
		org.junit.jupiter.api.Assertions.assertTrue(valids.forAll(Either::isRight));
		valids.forEachWithIndex(
				(r, i) ->
						org.junit.jupiter.api.Assertions.assertEquals(String.valueOf(i + 1), r.get().getId1()));
	}

	// end::batch-bean-multikey-demo[]

	@DisplayName("FailForDuplicates NOT configured. FAIL: NullValidatables, FILTER_ONLY: Duplicates")
	@Test
	void failNullValidatablesAndFilterDuplicates() {
		final List<UtilsBean> nullValidatables = List.of(null, null);
		final var duplicateValidatables =
				List.of(
						new UtilsBean("802xx000001ni4xAAA"),
						new UtilsBean("802xx000001ni4xAAA"),
						new UtilsBean("802xx000001ni4xAAA"));
		final var validatables =
				nullValidatables
						.appendAll(duplicateValidatables)
						.appendAll(List.of(new UtilsBean("1"), new UtilsBean("2"), new UtilsBean("3")));

		final var batchValidationConfig =
				BatchValidationConfig.<UtilsBean, ValidationFailure>toValidate()
						.findAndFilterDuplicatesConfig(
								FilterDuplicatesConfig.<UtilsBean, ValidationFailure>toValidate()
										.findAndFilterDuplicatesWith(
												container -> container.getId() == null ? null : container.getId())
										.andFailNullKeysWith(NOTHING_TO_VALIDATE))
						.prepare();
		final var results =
				VadorBatch.validateAndFailFastForEach(
						validatables.toJavaList(),
						bean -> bean == null ? "nullBean" : bean.getId(),
						batchValidationConfig);

		Assertions.assertThat(results).hasSize(5);
		var vavrResults = List.ofAll(results);
		final var failedInvalids = vavrResults.take(2);
		Assertions.assertThat(failedInvalids).allMatch(r -> r.getLeft()._2 == NOTHING_TO_VALIDATE);

		final var valids = vavrResults.drop(2);
		org.junit.jupiter.api.Assertions.assertTrue(valids.forAll(Either::isRight));
		valids.forEachWithIndex(
				(r, i) ->
						org.junit.jupiter.api.Assertions.assertEquals(String.valueOf(i + 1), r.get().getId()));
	}

	@DisplayName(
			"FailForDuplicates NOT configured. FAIL: NullValidatables, FAIL: Null Keys, FILTER_ONLY: Duplicates")
	@Test
	void failNullValidatablesAndNullKeysAndFilterDuplicates() {
		final List<UtilsBean> invalidValidatables = List.of(null, null);
		final var duplicateValidatables =
				List.of(
						new UtilsBean("802xx000001ni4xAAA"),
						new UtilsBean("802xx000001ni4xAAA"),
						new UtilsBean("802xx000001ni4xAAA"));
		final var validatablesWithNullKeys = List.of(new UtilsBean(null), new UtilsBean(null));
		final var validatables =
				invalidValidatables
						.appendAll(duplicateValidatables)
						.appendAll(validatablesWithNullKeys)
						.appendAll(List.of(new UtilsBean("1"), new UtilsBean("2"), new UtilsBean("3")));

		final var batchValidationConfig =
				BatchValidationConfig.<UtilsBean, ValidationFailure>toValidate()
						.findAndFilterDuplicatesConfig(
								FilterDuplicatesConfig.<UtilsBean, ValidationFailure>toValidate()
										.findAndFilterDuplicatesWith(
												container -> container.getId() == null ? null : container.getId())
										.andFailNullKeysWith(NULL_KEY))
						.prepare();
		final var results =
				List.ofAll(
						findAndFilterInvalids(
								validatables.toJavaList(),
								NOTHING_TO_VALIDATE,
								batchValidationConfig.getFindAndFilterDuplicatesConfigs()));

		Assertions.assertThat(results).hasSize(validatables.size() - duplicateValidatables.size());
		final var failedInvalids = results.take(2);
		Assertions.assertThat(failedInvalids).allMatch(r -> r.getLeft() == NULL_KEY);

		final var nullKeyInvalids = results.drop(2).take(2);
		Assertions.assertThat(nullKeyInvalids).allMatch(r -> r.getLeft() == NULL_KEY);

		final var valids = results.drop(4);
		org.junit.jupiter.api.Assertions.assertTrue(valids.forAll(Either::isRight));
		valids.forEachWithIndex(
				(r, i) ->
						org.junit.jupiter.api.Assertions.assertEquals(String.valueOf(i + 1), r.get().getId()));
	}

	@DisplayName(
			"FailForDuplicates, FailForNullKeys NOT configured. FAIL: Null Validatables, PASS: Null Keys, FILTER_ONLY: Duplicates")
	@Test
	void failInvalidatablesAndPassNullKeysAndFilterDuplicates() {
		final List<UtilsBean> invalidValidatables = List.of(null, null);
		final var duplicateValidatables =
				List.of(
						new UtilsBean("802xx000001ni4xAAA"),
						new UtilsBean("802xx000001ni4xAAA"),
						new UtilsBean("802xx000001ni4xAAA"));
		final var validatablesWithNullKeys = List.of(new UtilsBean(null), new UtilsBean(null));
		final var validatables =
				invalidValidatables
						.appendAll(duplicateValidatables)
						.appendAll(validatablesWithNullKeys)
						.appendAll(List.of(new UtilsBean("1"), new UtilsBean("2"), new UtilsBean("3")));

		final var batchValidationConfig =
				BatchValidationConfig.<UtilsBean, ValidationFailure>toValidate()
						.findAndFilterDuplicatesConfig(
								FilterDuplicatesConfig.<UtilsBean, ValidationFailure>toValidate()
										.findAndFilterDuplicatesWith(
												container -> container.getId() == null ? null : container.getId()))
						.prepare();
		final var results =
				List.ofAll(
						findAndFilterInvalids(
								validatables.toJavaList(),
								NOTHING_TO_VALIDATE,
								batchValidationConfig.getFindAndFilterDuplicatesConfigs()));

		Assertions.assertThat(results).hasSize(validatables.size() - duplicateValidatables.size());
		final var skippedNullValidatables = results.take(2);
		Assertions.assertThat(skippedNullValidatables)
				.allMatch(Either::isRight)
				.allMatch(r -> r.get() == null);

		final var nullKeyInvalids = results.drop(2).take(2);
		Assertions.assertThat(nullKeyInvalids).allMatch(r -> r.get().equals(new UtilsBean(null)));

		final var valids = results.drop(4);
		org.junit.jupiter.api.Assertions.assertTrue(valids.forAll(Either::isRight));
		valids.forEachWithIndex(
				(r, i) ->
						org.junit.jupiter.api.Assertions.assertEquals(String.valueOf(i + 1), r.get().getId()));
	}

	@DisplayName("First Failure : Null validatable")
	@Test
	void filterInvalidatablesAndFailDuplicatesForAllOrNoneNullValidatables() {
		final List<UtilsBean> invalidValidatables = List.of(null, null);
		final var duplicateValidatables =
				List.of(
						new UtilsBean("802xx000001ni4xAAA"),
						new UtilsBean("802xx000001ni4x"),
						new UtilsBean("802xx000001ni4x"));
		final var validatables =
				invalidValidatables
						.appendAll(duplicateValidatables)
						.appendAll(List.of(new UtilsBean("1"), new UtilsBean("2"), new UtilsBean("3")));

		final var batchValidationConfig =
				BatchValidationConfig.<UtilsBean, ValidationFailure>toValidate()
						.findAndFilterDuplicatesConfig(
								FilterDuplicatesConfig.<UtilsBean, ValidationFailure>toValidate()
										.findAndFilterDuplicatesWith(UtilsBean::getId)
										.andFailDuplicatesWith(DUPLICATE_ITEM))
						.prepare();
		final var result =
				findFirstInvalid(
						validatables.toJavaList(),
						NOTHING_TO_VALIDATE,
						batchValidationConfig.getFindAndFilterDuplicatesConfigs());
		Assertions.assertThat(result).contains(NOTHING_TO_VALIDATE);
	}

	@Test
	void filterInvalidatablesAndDuplicatesForAllOrNone() {
		final var duplicateValidatables =
				List.of(new UtilsBean("0"), new UtilsBean("0"), new UtilsBean("0"));
		final var validatables =
				duplicateValidatables.appendAll(
						List.of(new UtilsBean("1"), new UtilsBean("2"), new UtilsBean("3")));
		final var batchValidationConfig =
				BatchValidationConfig.<UtilsBean, ValidationFailure>toValidate()
						.findAndFilterDuplicatesConfig(
								FilterDuplicatesConfig.<UtilsBean, ValidationFailure>toValidate()
										.findAndFilterDuplicatesWith(UtilsBean::getId))
						.prepare();
		final var result =
				findFirstInvalid(
						validatables.toJavaList(),
						NOTHING_TO_VALIDATE,
						batchValidationConfig.getFindAndFilterDuplicatesConfigs());
		Assertions.assertThat(result).isEmpty();
	}

	@Test
	void filterInvalidatablesAndDuplicatesAndFailNullKeysForAllOrNone() {
		final var duplicateValidatables =
				List.of(new UtilsBean("0"), new UtilsBean("0"), new UtilsBean("0"));
		final var nullKeyValidatables = List.of(new UtilsBean(null), new UtilsBean(null));
		final var validatables =
				duplicateValidatables
						.appendAll(nullKeyValidatables)
						.appendAll(List.of(new UtilsBean("1"), new UtilsBean("2"), new UtilsBean("3")));
		final var batchValidationConfig =
				BatchValidationConfig.<UtilsBean, ValidationFailure>toValidate()
						.findAndFilterDuplicatesConfig(
								FilterDuplicatesConfig.<UtilsBean, ValidationFailure>toValidate()
										.findAndFilterDuplicatesWith(
												container -> container.getId() == null ? null : container.getId())
										.andFailNullKeysWith(NULL_KEY))
						.prepare();
		final var result =
				findFirstInvalid(
						validatables.toJavaList(),
						NOTHING_TO_VALIDATE,
						batchValidationConfig.getFindAndFilterDuplicatesConfigs());
		Assertions.assertThat(result).contains(NULL_KEY);
	}

	@Test
	void filterInvalidatablesAndDuplicatesForAllOrNoneDuplicate() {
		final var duplicateValidatables =
				List.of(new UtilsBean("0"), new UtilsBean("0"), new UtilsBean("0"));
		final var validatables =
				duplicateValidatables.appendAll(
						List.of(new UtilsBean("1"), new UtilsBean("2"), new UtilsBean("3")));

		final var batchValidationConfig =
				BatchValidationConfig.<UtilsBean, ValidationFailure>toValidate()
						.findAndFilterDuplicatesConfig(
								FilterDuplicatesConfig.<UtilsBean, ValidationFailure>toValidate()
										.findAndFilterDuplicatesWith(UtilsBean::getId)
										.andFailDuplicatesWith(DUPLICATE_ITEM))
						.prepare();
		final var result =
				findFirstInvalid(
						validatables.toJavaList(),
						NOTHING_TO_VALIDATE,
						batchValidationConfig.getFindAndFilterDuplicatesConfigs());
		Assertions.assertThat(result).contains(DUPLICATE_ITEM);
	}

	@Test
	void filterInvalidatablesAndDuplicatesForAllOrNoneAllValid() {
		final var validatables = List.of(new UtilsBean("1"), new UtilsBean("2"), new UtilsBean("3"));
		final var batchValidationConfig =
				BatchValidationConfig.<UtilsBean, ValidationFailure>toValidate()
						.findAndFilterDuplicatesConfig(
								FilterDuplicatesConfig.<UtilsBean, ValidationFailure>toValidate()
										.findAndFilterDuplicatesWith(UtilsBean::getId)
										.andFailDuplicatesWith(DUPLICATE_ITEM))
						.prepare();
		final var result =
				findFirstInvalid(
						validatables.toJavaList(),
						NOTHING_TO_VALIDATE,
						batchValidationConfig.getFindAndFilterDuplicatesConfigs());
		Assertions.assertThat(result).isEmpty();
	}
}
