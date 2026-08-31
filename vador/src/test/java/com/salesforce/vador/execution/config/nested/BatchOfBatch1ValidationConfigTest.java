/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.execution.config.nested;

import static com.salesforce.vador.execution.VadorBatch.validateAndFailFastForAny;
import static com.salesforce.vador.execution.VadorBatch.validateAndFailFastForEach;
import static com.salesforce.vador.matchers.AnyMatchers.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static sample.consumer.failure.ValidationFailure.INVALID_COMBO_1;
import static sample.consumer.failure.ValidationFailure.INVALID_ITEM;
import static sample.consumer.failure.ValidationFailure.NONE;

import com.salesforce.vador.config.BatchOfBatch1ValidationConfig;
import com.salesforce.vador.config.BatchValidationConfig;
import com.salesforce.vador.execution.VadorBatch;
import io.vavr.Tuple;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.vavr.api.VavrAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;

class BatchOfBatch1ValidationConfigTest {

	// tag::batch-of-batch-1-demo[]
	@DisplayName(
			"FailFastForEach -> Root[batchOf(Items(batchOf(Beans))]) or like `List<Item<List<Bean>>`")
	@Test
	void batchOfBatch1FailFastForEach() {
		final var memberBatchValidationConfig =
				BatchValidationConfig.<BatchOfBatch1ValidationConfigBean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._2()
												.when(BatchOfBatch1ValidationConfigBean::getValue)
												.matches(is(1))
												.then(BatchOfBatch1ValidationConfigBean::getLabel)
												.shouldMatch(anyOf("1", "one"))
												.orFailWith(INVALID_COMBO_1))
						.prepare();
		final var itemBatchValidationConfig =
				BatchOfBatch1ValidationConfig
						.<BatchOfBatch1ValidationConfigItem, BatchOfBatch1ValidationConfigBean,
								ValidationFailure>
								toValidate()
						.shouldHaveFieldOrFailWith(BatchOfBatch1ValidationConfigItem::getId, INVALID_ITEM)
						.withMemberBatchValidationConfig(
								Tuple.of(
										BatchOfBatch1ValidationConfigItem::getBeanBatch, memberBatchValidationConfig))
						.prepare();

		final var invalidBean1 = new BatchOfBatch1ValidationConfigBean(1, "a");
		final var beanBatch1 = List.of(invalidBean1, new BatchOfBatch1ValidationConfigBean(1, "1"));
		final var invalidBean2 = new BatchOfBatch1ValidationConfigBean(2, "b");
		final var beanBatch2 = List.of(invalidBean2, new BatchOfBatch1ValidationConfigBean(2, "2"));
		final var allValidBeanBatch3 =
				List.of(
						new BatchOfBatch1ValidationConfigBean(3, "three"),
						new BatchOfBatch1ValidationConfigBean(3, "3"));
		final var invalidItem = new BatchOfBatch1ValidationConfigItem("", beanBatch2);
		final var itemsBatch =
				List.of(
						new BatchOfBatch1ValidationConfigItem("item-1", beanBatch1),
						invalidItem,
						new BatchOfBatch1ValidationConfigItem("item-3", allValidBeanBatch3));
		final var root = new BatchOfBatch1ValidationConfigRoot(itemsBatch);

		final var results =
				validateAndFailFastForEach(root.getItemsBatch(), itemBatchValidationConfig, NONE);
		assertThat(results).hasSize(3);

		final var result1 = results.get(0);
		VavrAssertions.assertThat(result1).isLeft();
		final var failure = result1.getLeft();
		assertThat(failure.getContainerFailure()).isNull();
		assertThat(failure.getBatchMemberFailures()).containsExactly(INVALID_COMBO_1);

		final var result2 = results.get(1);
		VavrAssertions.assertThat(result2).isLeft();
		final var failure2 = result2.getLeft();
		assertThat(failure2.getContainerFailure()).isEqualTo(INVALID_ITEM);
		assertThat(failure2.getBatchMemberFailures()).isEmpty();

		final var result3 = results.get(2);
		VavrAssertions.assertThat(result3).isRight();
	}

	// end::batch-of-batch-1-demo[]

	@DisplayName(
			"FailFastForEach with Pair -> Root[batchOf(Items(batchOf(Beans))]) or like `List<Item<List<Bean>>`")
	@Test
	void batchOfBatch1FailFastForEachWithPair() {
		final var memberBatchValidationConfig =
				BatchValidationConfig.<BatchOfBatch1ValidationConfigBean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._2()
												.when(BatchOfBatch1ValidationConfigBean::getValue)
												.matches(is(1))
												.then(BatchOfBatch1ValidationConfigBean::getLabel)
												.shouldMatch(anyOf("1", "one"))
												.orFailWith(INVALID_COMBO_1))
						.prepare();
		final var itemBatchValidationConfig =
				BatchOfBatch1ValidationConfig
						.<BatchOfBatch1ValidationConfigItem, BatchOfBatch1ValidationConfigBean,
								ValidationFailure>
								toValidate()
						.shouldHaveFieldOrFailWith(BatchOfBatch1ValidationConfigItem::getId, INVALID_ITEM)
						.withMemberBatchValidationConfig(
								Tuple.of(
										BatchOfBatch1ValidationConfigItem::getBeanBatch, memberBatchValidationConfig))
						.prepare();

		final var invalidBean1 = new BatchOfBatch1ValidationConfigBean(1, "a");
		final var beanBatch1 = List.of(invalidBean1, new BatchOfBatch1ValidationConfigBean(1, "1"));
		final var invalidBean2 = new BatchOfBatch1ValidationConfigBean(2, "b");
		final var beanBatch2 = List.of(invalidBean2, new BatchOfBatch1ValidationConfigBean(2, "2"));
		final var allValidBeanBatch3 =
				List.of(
						new BatchOfBatch1ValidationConfigBean(3, "three"),
						new BatchOfBatch1ValidationConfigBean(3, "3"));
		final var invalidItem = new BatchOfBatch1ValidationConfigItem("", beanBatch2);
		final var itemsBatch =
				List.of(
						new BatchOfBatch1ValidationConfigItem("item-1", beanBatch1),
						invalidItem,
						new BatchOfBatch1ValidationConfigItem("item-3", allValidBeanBatch3));
		final var root = new BatchOfBatch1ValidationConfigRoot(itemsBatch);

		final var results =
				VadorBatch.validateAndFailFastForEach(
						root.getItemsBatch(),
						itemBatchValidationConfig,
						BatchOfBatch1ValidationConfigItem::getId,
						BatchOfBatch1ValidationConfigBean::getValue);
		assertThat(results).hasSize(3);

		final var result1 = results.get(0);
		VavrAssertions.assertThat(result1).isLeft();
		final var failure = result1.getLeft();
		assertThat(failure.getContainerFailure()).isNull();
		assertThat(failure.getBatchMemberFailures()).containsExactly(Tuple.of(1, INVALID_COMBO_1));

		final var result2 = results.get(1);
		VavrAssertions.assertThat(result2).isLeft();
		final var failure2 = result2.getLeft();
		assertThat(failure2.getContainerFailure()).isEqualTo(Tuple.of("", INVALID_ITEM));
		assertThat(failure2.getBatchMemberFailures()).isEmpty();

		final var result3 = results.get(2);
		VavrAssertions.assertThat(result3).isRight();
	}

	@DisplayName(
			"FailFastForAny with Invalid Container -> Root[batchOf(Items(batchOf(Beans))]) or like `List<Item<List<Bean>>`")
	@Test
	void batchOfBatch1FailFastForAnyWithInvalidContainer() {
		final var memberBatchValidationConfig =
				BatchValidationConfig.<BatchOfBatch1ValidationConfigBean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._2()
												.when(BatchOfBatch1ValidationConfigBean::getValue)
												.matches(is(1))
												.then(BatchOfBatch1ValidationConfigBean::getLabel)
												.shouldMatch(anyOf("1", "one"))
												.orFailWith(INVALID_COMBO_1))
						.prepare();
		final var itemBatchValidationConfig =
				BatchOfBatch1ValidationConfig
						.<BatchOfBatch1ValidationConfigItem, BatchOfBatch1ValidationConfigBean,
								ValidationFailure>
								toValidate()
						.shouldHaveFieldOrFailWith(BatchOfBatch1ValidationConfigItem::getId, INVALID_ITEM)
						.withMemberBatchValidationConfig(
								Tuple.of(
										BatchOfBatch1ValidationConfigItem::getBeanBatch, memberBatchValidationConfig))
						.prepare();

		final var bean1 = new BatchOfBatch1ValidationConfigBean(1, "one");
		final var beanBatch1 = List.of(bean1, new BatchOfBatch1ValidationConfigBean(1, "1"));
		final var invalidBean = new BatchOfBatch1ValidationConfigBean(1, "a");
		final var beanBatchWithInvalidBean =
				List.of(invalidBean, new BatchOfBatch1ValidationConfigBean(2, "2"));
		final var allValidBeanBatch3 =
				List.of(
						new BatchOfBatch1ValidationConfigBean(3, "three"),
						new BatchOfBatch1ValidationConfigBean(3, "3"));
		final var invalidItem = new BatchOfBatch1ValidationConfigItem("", beanBatchWithInvalidBean);
		final var itemsBatch =
				List.of(
						new BatchOfBatch1ValidationConfigItem("item-1", beanBatch1),
						invalidItem,
						new BatchOfBatch1ValidationConfigItem("item-3", allValidBeanBatch3));
		final var root = new BatchOfBatch1ValidationConfigRoot(itemsBatch);

		final var result = validateAndFailFastForAny(root.getItemsBatch(), itemBatchValidationConfig);
		Assertions.assertThat(result).isPresent();
		Assertions.assertThat(result.get()).isEqualTo(INVALID_ITEM);
	}

	@DisplayName(
			"FailFastForAny with Invalid Member -> Root[batchOf(Items(batchOf(Beans))]) or like `List<Item<List<Bean>>`")
	@Test
	void batchOfBatch1FailFastForAnyWithInvalidMember() {
		final var memberBatchValidationConfig =
				BatchValidationConfig.<BatchOfBatch1ValidationConfigBean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._2()
												.when(BatchOfBatch1ValidationConfigBean::getValue)
												.matches(is(1))
												.then(BatchOfBatch1ValidationConfigBean::getLabel)
												.shouldMatch(anyOf("1", "one"))
												.orFailWith(INVALID_COMBO_1))
						.prepare();
		final var itemBatchValidationConfig =
				BatchOfBatch1ValidationConfig
						.<BatchOfBatch1ValidationConfigItem, BatchOfBatch1ValidationConfigBean,
								ValidationFailure>
								toValidate()
						.shouldHaveFieldOrFailWith(BatchOfBatch1ValidationConfigItem::getId, INVALID_ITEM)
						.withMemberBatchValidationConfig(
								Tuple.of(
										BatchOfBatch1ValidationConfigItem::getBeanBatch, memberBatchValidationConfig))
						.prepare();

		final var beanBatch1 =
				List.of(
						new BatchOfBatch1ValidationConfigBean(1, "one"),
						new BatchOfBatch1ValidationConfigBean(1, "1"));
		final var invalidBean = new BatchOfBatch1ValidationConfigBean(1, "a");
		final var beanBatchWithInvalidBean =
				List.of(invalidBean, new BatchOfBatch1ValidationConfigBean(2, "2"));
		final var beanBatch3 =
				List.of(
						new BatchOfBatch1ValidationConfigBean(3, "three"),
						new BatchOfBatch1ValidationConfigBean(3, "t"));
		final var itemsBatch =
				List.of(
						new BatchOfBatch1ValidationConfigItem("item-1", beanBatch1),
						new BatchOfBatch1ValidationConfigItem("item-2", beanBatchWithInvalidBean),
						new BatchOfBatch1ValidationConfigItem("item-3", beanBatch3));
		final var root = new BatchOfBatch1ValidationConfigRoot(itemsBatch);

		final var result = validateAndFailFastForAny(root.getItemsBatch(), itemBatchValidationConfig);
		Assertions.assertThat(result).isPresent();
		Assertions.assertThat(result.get()).isEqualTo(INVALID_COMBO_1);
	}

	@DisplayName(
			"FailFastForAny with Pair with invalid Container -> Root[batchOf(Items(batchOf(Beans))]) or like `List<Item<List<Bean>>`")
	@Test
	void batchOfBatch1FailFastForAnyWithPairWithInvalidContainer() {
		final var memberBatchValidationConfig =
				BatchValidationConfig.<BatchOfBatch1ValidationConfigBean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._2()
												.when(BatchOfBatch1ValidationConfigBean::getValue)
												.matches(is(1))
												.then(BatchOfBatch1ValidationConfigBean::getLabel)
												.shouldMatch(anyOf("1", "one"))
												.orFailWith(INVALID_COMBO_1))
						.prepare();
		final var itemBatchValidationConfig =
				BatchOfBatch1ValidationConfig
						.<BatchOfBatch1ValidationConfigItem, BatchOfBatch1ValidationConfigBean,
								ValidationFailure>
								toValidate()
						.shouldHaveFieldOrFailWith(BatchOfBatch1ValidationConfigItem::getId, INVALID_ITEM)
						.withMemberBatchValidationConfig(
								Tuple.of(
										BatchOfBatch1ValidationConfigItem::getBeanBatch, memberBatchValidationConfig))
						.prepare();

		final var beanBatch1 =
				List.of(
						new BatchOfBatch1ValidationConfigBean(1, "one"),
						new BatchOfBatch1ValidationConfigBean(1, "1"));
		final var invalidBean = new BatchOfBatch1ValidationConfigBean(1, "a");
		final var beanBatchWithInvalidBean =
				List.of(invalidBean, new BatchOfBatch1ValidationConfigBean(2, "2"));
		final var beanBatch3 =
				List.of(
						new BatchOfBatch1ValidationConfigBean(3, "three"),
						new BatchOfBatch1ValidationConfigBean(3, "t"));
		final var invalidItem = new BatchOfBatch1ValidationConfigItem("", beanBatchWithInvalidBean);
		final var itemsBatch =
				List.of(
						new BatchOfBatch1ValidationConfigItem("item-1", beanBatch1),
						invalidItem,
						new BatchOfBatch1ValidationConfigItem("item-3", beanBatch3));
		final var root = new BatchOfBatch1ValidationConfigRoot(itemsBatch);

		final var result =
				VadorBatch.validateAndFailFastForAny(
						root.getItemsBatch(),
						BatchOfBatch1ValidationConfigItem::getId,
						BatchOfBatch1ValidationConfigBean::getLabel,
						itemBatchValidationConfig);
		assertThat(result).isPresent();

		assertThat(result.get().isContainerValid()).isFalse();
		assertThat(result.get().getContainerFailure()).contains(Tuple.of("", INVALID_ITEM));
		assertThat(result.get().isBatchMemberValid()).isTrue();
		assertThat(result.get().getBatchMemberFailure()).isEmpty();
	}

	@DisplayName(
			"FailFastForAny with Pair with Invalid Member -> Root[batchOf(Items(batchOf(Beans))]) or like `List<Item<List<Bean>>`")
	@Test
	void batchOfBatch1FailFastForAnyWithPairWithInvalidMember() {
		final var memberBatchValidationConfig =
				BatchValidationConfig.<BatchOfBatch1ValidationConfigBean, ValidationFailure>toValidate()
						.withSpec(
								spec ->
										spec._2()
												.when(BatchOfBatch1ValidationConfigBean::getValue)
												.matches(is(1))
												.then(BatchOfBatch1ValidationConfigBean::getLabel)
												.shouldMatch(anyOf("1", "one"))
												.orFailWith(INVALID_COMBO_1))
						.prepare();
		final var itemBatchValidationConfig =
				BatchOfBatch1ValidationConfig
						.<BatchOfBatch1ValidationConfigItem, BatchOfBatch1ValidationConfigBean,
								ValidationFailure>
								toValidate()
						.shouldHaveFieldOrFailWith(BatchOfBatch1ValidationConfigItem::getId, INVALID_ITEM)
						.withMemberBatchValidationConfig(
								Tuple.of(
										BatchOfBatch1ValidationConfigItem::getBeanBatch, memberBatchValidationConfig))
						.prepare();

		final var beanBatch1 =
				List.of(
						new BatchOfBatch1ValidationConfigBean(1, "one"),
						new BatchOfBatch1ValidationConfigBean(1, "1"));
		final var invalidBean = new BatchOfBatch1ValidationConfigBean(1, "a");
		final var beanBatchWithInvalidBean =
				List.of(invalidBean, new BatchOfBatch1ValidationConfigBean(2, "2"));
		final var beanBatch3 =
				List.of(
						new BatchOfBatch1ValidationConfigBean(3, "three"),
						new BatchOfBatch1ValidationConfigBean(3, "t"));
		final var itemsBatch =
				List.of(
						new BatchOfBatch1ValidationConfigItem("item-2", beanBatch1),
						new BatchOfBatch1ValidationConfigItem("item-2", beanBatchWithInvalidBean),
						new BatchOfBatch1ValidationConfigItem("item-3", beanBatch3));
		final var root = new BatchOfBatch1ValidationConfigRoot(itemsBatch);

		final var result =
				VadorBatch.validateAndFailFastForAny(
						root.getItemsBatch(),
						BatchOfBatch1ValidationConfigItem::getId,
						BatchOfBatch1ValidationConfigBean::getValue,
						itemBatchValidationConfig);
		assertThat(result).isPresent();
		assertThat(result.get().isContainerValid()).isTrue();
		assertThat(result.get().getContainerFailure()).isEmpty();
		assertThat(result.get().isBatchMemberValid()).isFalse();
		assertThat(result.get().getBatchMemberFailure()).contains(Tuple.of(1, INVALID_COMBO_1));
	}
}
