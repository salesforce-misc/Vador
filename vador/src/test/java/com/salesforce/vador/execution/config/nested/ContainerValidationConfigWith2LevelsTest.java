/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.execution.config.nested;

import static com.salesforce.vador.execution.config.nested.ContainerValidationConfigWith2LevelsContainerLevel1WithMultiBatch.Fields.beanBatch;
import static com.salesforce.vador.execution.config.nested.ContainerValidationConfigWith2LevelsContainerLevel1WithMultiBatch.Fields.containerLevel2Batch;
import static com.salesforce.vador.execution.config.nested.ContainerValidationConfigWith2LevelsContainerRootWithMultiContainerBatch.Fields.containerLevel1Batch1;
import static com.salesforce.vador.execution.config.nested.ContainerValidationConfigWith2LevelsContainerRootWithMultiContainerBatch.Fields.containerLevel1Batch2;
import static org.assertj.core.api.Assertions.assertThat;
import static sample.consumer.failure.ValidationFailure.MAX_BATCH_SIZE_EXCEEDED_LEVEL_2;
import static sample.consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_LEVEL_1;
import static sample.consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_LEVEL_2;
import static sample.consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_ROOT_LEVEL;
import static sample.consumer.failure.ValidationFailure.NONE;

import com.salesforce.vador.config.container.ContainerValidationConfig;
import com.salesforce.vador.config.container.ContainerValidationConfigWith2Levels;
import com.salesforce.vador.execution.Vador;
import com.salesforce.vador.execution.VadorBatch;
import io.vavr.Tuple;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;

class ContainerValidationConfigWith2LevelsTest {

	// tag::container-config-level-2-demo[]
	@DisplayName(
			"Container with 2 levels: (ContainerRoot -> ContainerLevel1 -> ContainerLevel2) + Container with next 1 level: (ContainerLevel1 -> ContainerLevel2)")
	@Test
	void containerValidationConfigWith2Levels1() {
		final var containerRootValidationConfigFor2Levels =
				ContainerValidationConfigWith2Levels
						.<ContainerValidationConfigWith2LevelsContainerRoot,
								ContainerValidationConfigWith2LevelsContainerLevel1, ValidationFailure>
								toValidate()
						.withBatchMember(
								ContainerValidationConfigWith2LevelsContainerRoot::getContainerLevel1Batch)
						.shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_ROOT_LEVEL))
						.withScopeOf1LevelDeep(
								ContainerValidationConfig
										.<ContainerValidationConfigWith2LevelsContainerLevel1, ValidationFailure>
												toValidate()
										.withBatchMember(
												ContainerValidationConfigWith2LevelsContainerLevel1
														::getContainerLevel2Batch)
										.shouldHaveMinBatchSizeOrFailWith(Tuple.of(5, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
										.prepare())
						.prepare();
		final var containerLevel1ValidationConfig =
				ContainerValidationConfig
						.<ContainerValidationConfigWith2LevelsContainerLevel1, ValidationFailure>toValidate()
						.withBatchMember(
								ContainerValidationConfigWith2LevelsContainerLevel1::getContainerLevel2Batch)
						.shouldHaveMinBatchSizeOrFailWith(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_2))
						.withContainerValidator(ignore -> NONE, NONE)
						.prepare();

		// level-3
		final var beanBatch1 = List.of(new ContainerValidationConfigWith2LevelsBean());
		final var beanBatch2 = List.of(new ContainerValidationConfigWith2LevelsBean());
		final var beanBatch3 = List.of(new ContainerValidationConfigWith2LevelsBean());
		final var beanBatch4 = List.of(new ContainerValidationConfigWith2LevelsBean());
		// level-2
		final var containerLevel2Batch1 =
				List.of(
						new ContainerValidationConfigWith2LevelsContainerLevel2(11, beanBatch1),
						new ContainerValidationConfigWith2LevelsContainerLevel2(12, beanBatch2));
		final var containerLevel2Batch2 =
				List.of(
						new ContainerValidationConfigWith2LevelsContainerLevel2(21, beanBatch3),
						new ContainerValidationConfigWith2LevelsContainerLevel2(22, beanBatch4));
		// level-1
		final var containerLevel1Batch =
				List.of(
						new ContainerValidationConfigWith2LevelsContainerLevel1(1, containerLevel2Batch1),
						new ContainerValidationConfigWith2LevelsContainerLevel1(2, containerLevel2Batch2));
		// root-level
		final var containerRoot =
				new ContainerValidationConfigWith2LevelsContainerRoot(containerLevel1Batch);

		final var result =
				Vador.validateAndFailFastForContainer(
								containerRoot, containerRootValidationConfigFor2Levels)
						.or(
								() ->
										VadorBatch.validateAndFailFastForContainer(
												containerLevel1Batch, containerLevel1ValidationConfig));

		assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET_LEVEL_1);
	}

	// end::container-config-level-2-demo[]

	@DisplayName(
			"Container with 2 levels: (ContainerRoot -> ContainerLevel1 -> ContainerLevel2) + Container with next 2 levels: (ContainerLevel1 -> ContainerLevel2 -> Bean)")
	@Test
	void containerValidationConfigWithScopeOf2LevelsDeep() {
		final var containerRootValidationConfigWithScopeOf1LevelDeep =
				ContainerValidationConfigWith2Levels
						.<ContainerValidationConfigWith2LevelsContainerRoot,
								ContainerValidationConfigWith2LevelsContainerLevel1, ValidationFailure>
								toValidate()
						.withBatchMember(
								ContainerValidationConfigWith2LevelsContainerRoot::getContainerLevel1Batch)
						.shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_ROOT_LEVEL))
						.withScopeOf1LevelDeep(
								ContainerValidationConfig
										.<ContainerValidationConfigWith2LevelsContainerLevel1, ValidationFailure>
												toValidate()
										.withBatchMember(
												ContainerValidationConfigWith2LevelsContainerLevel1
														::getContainerLevel2Batch)
										.shouldHaveMinBatchSizeOrFailWith(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
										.prepare())
						.prepare();
		final var containerLevel1ValidationConfigWithScopeOf1LevelDeep =
				ContainerValidationConfigWith2Levels
						.<ContainerValidationConfigWith2LevelsContainerLevel1,
								ContainerValidationConfigWith2LevelsContainerLevel2, ValidationFailure>
								toValidate()
						.withBatchMember(
								ContainerValidationConfigWith2LevelsContainerLevel1::getContainerLevel2Batch)
						.shouldHaveMinBatchSizeOrFailWith(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_2))
						.withScopeOf1LevelDeep(
								ContainerValidationConfig
										.<ContainerValidationConfigWith2LevelsContainerLevel2, ValidationFailure>
												toValidate()
										.withBatchMember(
												ContainerValidationConfigWith2LevelsContainerLevel2::getBeanBatch)
										.shouldHaveMaxBatchSizeOrFailWith(Tuple.of(3, MAX_BATCH_SIZE_EXCEEDED_LEVEL_2))
										.prepare())
						.prepare();

		// level-3
		final var beanBatch1 =
				List.of(
						new ContainerValidationConfigWith2LevelsBean(),
						new ContainerValidationConfigWith2LevelsBean());
		final var beanBatch2 =
				List.of(
						new ContainerValidationConfigWith2LevelsBean(),
						new ContainerValidationConfigWith2LevelsBean());
		final var beanBatch3 = List.of(new ContainerValidationConfigWith2LevelsBean());
		final var beanBatch4 = List.of(new ContainerValidationConfigWith2LevelsBean());
		// level-2
		final var level2ContainerBatch1 =
				List.of(
						new ContainerValidationConfigWith2LevelsContainerLevel2(11, beanBatch1),
						new ContainerValidationConfigWith2LevelsContainerLevel2(12, beanBatch2));
		final var level2ContainerBatch2 =
				List.of(
						new ContainerValidationConfigWith2LevelsContainerLevel2(21, beanBatch3),
						new ContainerValidationConfigWith2LevelsContainerLevel2(22, beanBatch4));
		// level-1
		final var level1ContainerBatch =
				List.of(
						new ContainerValidationConfigWith2LevelsContainerLevel1(1, level2ContainerBatch1),
						new ContainerValidationConfigWith2LevelsContainerLevel1(2, level2ContainerBatch2));
		// root
		final var rootContainer =
				new ContainerValidationConfigWith2LevelsContainerRoot(level1ContainerBatch);

		final var result =
				Vador.validateAndFailFastForContainer(
								rootContainer, containerRootValidationConfigWithScopeOf1LevelDeep)
						.or(
								() ->
										VadorBatch.validateAndFailFastForContainer(
												level1ContainerBatch,
												containerLevel1ValidationConfigWithScopeOf1LevelDeep));

		assertThat(result).contains(MAX_BATCH_SIZE_EXCEEDED_LEVEL_2);
	}

	@DisplayName(
			"With Pair for Invalid mapper, Container batch with 2 levels: (ContainerLevel1 -> ContainerLevel2 -> Bean)")
	@Test
	void containerValidationConfigWithPairForInvalidMapper() {
		final var containerLevel1ValidationConfigWithScopeOf1LevelDeep =
				ContainerValidationConfigWith2Levels
						.<ContainerValidationConfigWith2LevelsContainerLevel1,
								ContainerValidationConfigWith2LevelsContainerLevel2, ValidationFailure>
								toValidate()
						.withBatchMember(
								ContainerValidationConfigWith2LevelsContainerLevel1::getContainerLevel2Batch)
						.shouldHaveMinBatchSizeOrFailWith(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_2))
						.withScopeOf1LevelDeep(
								ContainerValidationConfig
										.<ContainerValidationConfigWith2LevelsContainerLevel2, ValidationFailure>
												toValidate()
										.withBatchMember(
												ContainerValidationConfigWith2LevelsContainerLevel2::getBeanBatch)
										.shouldHaveMaxBatchSizeOrFailWith(Tuple.of(3, MAX_BATCH_SIZE_EXCEEDED_LEVEL_2))
										.prepare())
						.prepare();

		// level-3
		final var beanBatch1 =
				List.of(
						new ContainerValidationConfigWith2LevelsBean(),
						new ContainerValidationConfigWith2LevelsBean());
		final var beanBatch2 =
				List.of(
						new ContainerValidationConfigWith2LevelsBean(),
						new ContainerValidationConfigWith2LevelsBean());
		final var beanBatch3 = List.of(new ContainerValidationConfigWith2LevelsBean());
		final var beanBatch4 = List.of(new ContainerValidationConfigWith2LevelsBean());
		// level-2
		final var level2ContainerBatch1 =
				List.of(
						new ContainerValidationConfigWith2LevelsContainerLevel2(11, beanBatch1),
						new ContainerValidationConfigWith2LevelsContainerLevel2(12, beanBatch2));
		final var level2ContainerBatch2 =
				List.of(
						new ContainerValidationConfigWith2LevelsContainerLevel2(21, beanBatch3),
						new ContainerValidationConfigWith2LevelsContainerLevel2(22, beanBatch4));
		// level-1
		final var level1ContainerBatch =
				List.of(
						new ContainerValidationConfigWith2LevelsContainerLevel1(1, level2ContainerBatch1),
						new ContainerValidationConfigWith2LevelsContainerLevel1(2, level2ContainerBatch2));

		final var result =
				VadorBatch.validateAndFailFastForContainer(
						level1ContainerBatch,
						ContainerValidationConfigWith2LevelsContainerLevel1::getId,
						containerLevel1ValidationConfigWithScopeOf1LevelDeep);

		assertThat(result).contains(Tuple.of(1, MAX_BATCH_SIZE_EXCEEDED_LEVEL_2));
	}

	@Test
	void getFieldNamesForBatch() {
		final var validationConfig =
				ContainerValidationConfigWith2Levels
						.<ContainerValidationConfigWith2LevelsContainerRootWithMultiContainerBatch,
								ContainerValidationConfigWith2LevelsContainerLevel1WithMultiBatch,
								ValidationFailure>
								toValidate()
						.withBatchMembers(
								List.of(
										ContainerValidationConfigWith2LevelsContainerRootWithMultiContainerBatch
												::getContainerLevel1Batch1,
										ContainerValidationConfigWith2LevelsContainerRootWithMultiContainerBatch
												::getContainerLevel1Batch2))
						.withScopeOf1LevelDeep(
								ContainerValidationConfig
										.<ContainerValidationConfigWith2LevelsContainerLevel1WithMultiBatch,
												ValidationFailure>
												toValidate()
										.withBatchMembers(
												List.of(
														ContainerValidationConfigWith2LevelsContainerLevel1WithMultiBatch
																::getContainerLevel2Batch,
														ContainerValidationConfigWith2LevelsContainerLevel1WithMultiBatch
																::getBeanBatch))
										.shouldHaveMinBatchSizeOrFailWith(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
										.prepare())
						.prepare();
		assertThat(
						validationConfig.getFieldNamesForBatchRootLevel(
								ContainerValidationConfigWith2LevelsContainerRootWithMultiContainerBatch.class))
				.containsExactly(containerLevel1Batch1, containerLevel1Batch2);
		assertThat(
						validationConfig.getFieldNamesForBatchLevel1(
								ContainerValidationConfigWith2LevelsContainerLevel1WithMultiBatch.class))
				.containsExactly(containerLevel2Batch, beanBatch);
	}
}
