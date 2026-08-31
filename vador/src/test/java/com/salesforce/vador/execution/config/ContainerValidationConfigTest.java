/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.execution.config;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static sample.consumer.failure.ValidationFailure.MAX_BATCH_SIZE_EXCEEDED;
import static sample.consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET;
import static sample.consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_LEVEL_1;
import static sample.consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_ROOT_LEVEL;
import static sample.consumer.failure.ValidationFailure.NONE;
import static sample.consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;

import com.salesforce.vador.config.container.ContainerValidationConfig;
import com.salesforce.vador.execution.Vador;
import com.salesforce.vador.execution.VadorBatch;
import com.salesforce.vador.execution.config.ContainerValidationConfigContainerWithMultiBatch.Fields;
import io.vavr.Tuple;
import io.vavr.control.Either;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;

class ContainerValidationConfigTest {

	@Test
	void failFastForHeaderConfigWithValidators() {
		final var containerValidationConfig =
				ContainerValidationConfig
						.<ContainerValidationConfigContainerLevel1, ValidationFailure>toValidate()
						.withBatchMember(ContainerValidationConfigContainerLevel1::getBeanBatch)
						.withContainerValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
						.prepare();
		final var batch = List.of(new ContainerValidationConfigBean());
		final var containerBean = new ContainerValidationConfigContainerLevel1(batch);
		final var result =
				Vador.validateAndFailFastForContainer(containerBean, containerValidationConfig);
		assertThat(result).contains(UNKNOWN_EXCEPTION);
	}

	@Test
	void failFastForHeaderConfigValidatorsCount() {
		final var containerValidationConfig =
				ContainerValidationConfig
						.<ContainerValidationConfigContainerLevel1, ValidationFailure>toValidate()
						.withBatchMember(ContainerValidationConfigContainerLevel1::getBeanBatch)
						.withContainerValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
						.withContainerValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
						.withContainerValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
						.prepare();
		Assertions.assertThat(containerValidationConfig.getContainerValidators()).hasSize(3);
	}

	@Test
	void failFastForHeaderConfigWithValidators2() {
		final var containerValidationConfig =
				ContainerValidationConfig
						.<ContainerValidationConfigContainerLevel1, ValidationFailure>toValidate()
						.withBatchMember(ContainerValidationConfigContainerLevel1::getBeanBatch)
						.withContainerValidator(ignore -> NONE, NONE)
						.prepare();
		final var batch = List.of(new ContainerValidationConfigBean());
		final var containerBean = new ContainerValidationConfigContainerLevel1(batch);
		final var result =
				Vador.validateAndFailFastForContainer(containerBean, containerValidationConfig);
		assertThat(result).isEmpty();
	}

	@Test
	void failFastForHeaderConfigMinBatchSize() {
		final var containerValidationConfig =
				ContainerValidationConfig
						.<ContainerValidationConfigContainerLevel1, ValidationFailure>toValidate()
						.withBatchMember(ContainerValidationConfigContainerLevel1::getBeanBatch)
						.shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET))
						.withContainerValidator(ignore -> NONE, NONE)
						.prepare();
		final var containerBean = new ContainerValidationConfigContainerLevel1(emptyList());
		final var result =
				Vador.validateAndFailFastForContainer(containerBean, containerValidationConfig);
		assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET);
	}

	@Test
	void failFastForHeaderConfigMinBatchSizeForBatch() {
		final var containerValidationConfig =
				ContainerValidationConfig
						.<ContainerValidationConfigContainerWithPair, ValidationFailure>toValidate()
						.withBatchMember(ContainerValidationConfigContainerWithPair::getBeanBatch)
						.shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET))
						.withContainerValidator(ignore -> NONE, NONE)
						.prepare();
		final var containerWithInvalidMember =
				new ContainerValidationConfigContainerWithPair(2, emptyList());
		final var containerBeanBatch =
				List.of(
						new ContainerValidationConfigContainerWithPair(
								1, List.of(new ContainerValidationConfigBean())),
						containerWithInvalidMember,
						new ContainerValidationConfigContainerWithPair(
								3, List.of(new ContainerValidationConfigBean())));
		final var result =
				VadorBatch.validateAndFailFastForContainer(containerBeanBatch, containerValidationConfig);
		assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET);
	}

	@Test
	void failFastForContainerConfigMinBatchSizeForBatchWithPair() {
		final var containerValidationConfig =
				ContainerValidationConfig
						.<ContainerValidationConfigContainerWithPair, ValidationFailure>toValidate()
						.withBatchMember(ContainerValidationConfigContainerWithPair::getBeanBatch)
						.shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET))
						.withContainerValidator(ignore -> NONE, NONE)
						.prepare();
		final var containerWithInvalidMember =
				new ContainerValidationConfigContainerWithPair(2, emptyList());
		final var containerBatch =
				List.of(
						new ContainerValidationConfigContainerWithPair(
								1, List.of(new ContainerValidationConfigBean())),
						containerWithInvalidMember,
						new ContainerValidationConfigContainerWithPair(
								3, List.of(new ContainerValidationConfigBean())));
		final var result =
				VadorBatch.validateAndFailFastForContainer(
						containerBatch,
						ContainerValidationConfigContainerWithPair::getId,
						containerValidationConfig);
		assertThat(result).contains(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET));
	}

	// tag::container-config-level-1-container-with-multi-batch-demo[]
	@Test
	void failFastForHeaderConfigBatchSizeForMultiBatch() {
		final var containerValidationConfig =
				ContainerValidationConfig
						.<ContainerValidationConfigContainerWithMultiBatch, ValidationFailure>toValidate()
						.withBatchMembers(
								List.of(
										ContainerValidationConfigContainerWithMultiBatch::getBatch1,
										ContainerValidationConfigContainerWithMultiBatch::getBatch2))
						.shouldHaveMinBatchSizeOrFailWith(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET))
						.shouldHaveMaxBatchSizeOrFailWith(Tuple.of(3, MAX_BATCH_SIZE_EXCEEDED))
						.prepare();
		final var containerBean =
				new ContainerValidationConfigContainerWithMultiBatch(
						emptyList(), List.of(new ContainerValidationConfigBean2()));
		final var result =
				Vador.validateAndFailFastForContainer(containerBean, containerValidationConfig);
		assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET);
	}

	// end::container-config-level-1-container-with-multi-batch-demo[]

	@Test
	void failFastForHeaderConfigMaxBatchSize() {
		final var containerValidationConfig =
				ContainerValidationConfig
						.<ContainerValidationConfigContainerLevel1, ValidationFailure>toValidate()
						.withBatchMember(ContainerValidationConfigContainerLevel1::getBeanBatch)
						.shouldHaveMaxBatchSizeOrFailWith(Tuple.of(0, MAX_BATCH_SIZE_EXCEEDED))
						.prepare();
		final var containerBean =
				new ContainerValidationConfigContainerLevel1(List.of(new ContainerValidationConfigBean()));
		final var result =
				Vador.validateAndFailFastForContainer(containerBean, containerValidationConfig);
		assertThat(result).contains(MAX_BATCH_SIZE_EXCEEDED);
	}

	@Test
	void headerWithFailure() {
		final var containerValidationConfig =
				ContainerValidationConfig
						.<ContainerValidationConfigContainerLevel1, ValidationFailure>toValidate()
						.withContainerValidatorEtrs(
								List.of(
										containerBean -> Either.right(NONE),
										containerBean -> Either.left(UNKNOWN_EXCEPTION),
										containerBean -> Either.right(NONE)))
						.withBatchMember(ContainerValidationConfigContainerLevel1::getBeanBatch)
						.prepare();
		final var result =
				Vador.validateAndFailFastForContainer(
						new ContainerValidationConfigContainerLevel1(emptyList()), containerValidationConfig);
		assertThat(result).contains(UNKNOWN_EXCEPTION);
	}

	@Test
	void getFieldNamesForBatch() {
		final var validationConfig =
				ContainerValidationConfig
						.<ContainerValidationConfigContainerWithMultiBatch, ValidationFailure>toValidate()
						.withBatchMembers(
								List.of(
										ContainerValidationConfigContainerWithMultiBatch::getBatch1,
										ContainerValidationConfigContainerWithMultiBatch::getBatch2))
						.prepare();
		assertThat(
						validationConfig.getFieldNamesForBatch(
								ContainerValidationConfigContainerWithMultiBatch.class))
				.containsExactly(Fields.batch1, Fields.batch2);
	}

	// tag::container-config-level-1-container-with-container-batch-demo[]
	@DisplayName(
			"Compose the validation results from a container with results from the Batch Container it contains")
	@Test
	void composeContainerValidationResults() {
		final var containerRootValidationConfig =
				ContainerValidationConfig
						.<ContainerValidationConfigContainerRoot, ValidationFailure>toValidate()
						.withBatchMember(ContainerValidationConfigContainerRoot::getContainerLevel1Batch)
						.shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_ROOT_LEVEL))
						.prepare();
		final var containerValidationConfig =
				ContainerValidationConfig
						.<ContainerValidationConfigContainerLevel1, ValidationFailure>toValidate()
						.withBatchMember(ContainerValidationConfigContainerLevel1::getBeanBatch)
						.shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
						.prepare();

		final var beanBatch = List.of(new ContainerValidationConfigBean());
		final var container2Batch =
				List.of(
						new ContainerValidationConfigContainerLevel1(beanBatch),
						new ContainerValidationConfigContainerLevel1(emptyList()));
		final var container1 = new ContainerValidationConfigContainerRoot(container2Batch);

		final var result =
				Vador.validateAndFailFastForContainer(container1, containerRootValidationConfig)
						.or(
								() ->
										VadorBatch.validateAndFailFastForContainer(
												container2Batch, containerValidationConfig));

		assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET_LEVEL_1);
	}

	// end::container-config-level-1-container-with-container-batch-demo[]

}
