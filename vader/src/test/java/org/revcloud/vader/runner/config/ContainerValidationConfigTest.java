/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.runner.config;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static sample.consumer.failure.ValidationFailure.MAX_BATCH_SIZE_EXCEEDED;
import static sample.consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET;
import static sample.consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_LEVEL_1;
import static sample.consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_ROOT_LEVEL;
import static sample.consumer.failure.ValidationFailure.NONE;
import static sample.consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;

import io.vavr.Tuple;
import io.vavr.control.Either;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.ContainerValidationConfig;
import org.revcloud.vader.runner.Vader;
import org.revcloud.vader.runner.VaderBatch;
import org.revcloud.vader.runner.config.ContainerValidationConfigTest.ContainerWithMultiBatch.Fields;
import sample.consumer.failure.ValidationFailure;

class ContainerValidationConfigTest {

  @Test
  void failFastForHeaderConfigWithValidators() {
    final var containerValidationConfig =
        ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMember(ContainerLevel1::getBeanBatch)
            .withContainerValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
            .prepare();
    final var batch = List.of(new Bean());
    final var headerBean = new ContainerLevel1(batch);
    final var result = Vader.validateAndFailFastForContainer(headerBean, containerValidationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Test
  void failFastForHeaderConfigValidatorsCount() {
    final var containerValidationConfig =
        ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMember(ContainerLevel1::getBeanBatch)
            .withContainerValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
            .withContainerValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
            .withContainerValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
            .prepare();
    assertThat(containerValidationConfig.getContainerValidators()).hasSize(3);
  }

  @Test
  void failFastForHeaderConfigWithValidators2() {
    final var containerValidationConfig =
        ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMember(ContainerLevel1::getBeanBatch)
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();
    final var batch = List.of(new Bean());
    final var headerBean = new ContainerLevel1(batch);
    final var result = Vader.validateAndFailFastForContainer(headerBean, containerValidationConfig);
    assertThat(result).isEmpty();
  }

  @Test
  void failFastForHeaderConfigMinBatchSize() {
    final var containerValidationConfig =
        ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMember(ContainerLevel1::getBeanBatch)
            .shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET))
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();
    final var headerBean = new ContainerLevel1(emptyList());
    final var result = Vader.validateAndFailFastForContainer(headerBean, containerValidationConfig);
    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET);
  }

  @Test
  void failFastForHeaderConfigMinBatchSizeForBatch() {
    final var containerValidationConfig =
        ContainerValidationConfig.<ContainerWithPair, ValidationFailure>toValidate()
            .withBatchMember(ContainerWithPair::getBeanBatch)
            .shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET))
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();
    final var containerWithInvalidMember = new ContainerWithPair(2, emptyList());
    final var headerBeanBatch =
        List.of(
            new ContainerWithPair(1, List.of(new Bean())),
            containerWithInvalidMember,
            new ContainerWithPair(3, List.of(new Bean())));
    final var result =
        VaderBatch.validateAndFailFastForContainer(headerBeanBatch, containerValidationConfig);
    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET);
  }

  @Test
  void failFastForContainerConfigMinBatchSizeForBatchWithPair() {
    final var containerValidationConfig =
        ContainerValidationConfig.<ContainerWithPair, ValidationFailure>toValidate()
            .withBatchMember(ContainerWithPair::getBeanBatch)
            .shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET))
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();
    final var containerWithInvalidMember = new ContainerWithPair(2, emptyList());
    final var containerBatch =
        List.of(
            new ContainerWithPair(1, List.of(new Bean())),
            containerWithInvalidMember,
            new ContainerWithPair(3, List.of(new Bean())));
    final var result =
        VaderBatch.validateAndFailFastForContainer(
            containerBatch, ContainerWithPair::getId, containerValidationConfig);
    assertThat(result).contains(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET));
  }

  // tag::container-config-level-1-container-with-multi-batch-demo[]
  @Test
  void failFastForHeaderConfigBatchSizeForMultiBatch() {
    final var containerValidationConfig =
        ContainerValidationConfig.<ContainerWithMultiBatch, ValidationFailure>toValidate()
            .withBatchMembers(
                List.of(ContainerWithMultiBatch::getBatch1, ContainerWithMultiBatch::getBatch2))
            .shouldHaveMinBatchSizeOrFailWith(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET))
            .shouldHaveMaxBatchSizeOrFailWith(Tuple.of(3, MAX_BATCH_SIZE_EXCEEDED))
            .prepare();
    final var headerBean = new ContainerWithMultiBatch(emptyList(), List.of(new Bean2()));
    final var result = Vader.validateAndFailFastForContainer(headerBean, containerValidationConfig);
    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET);
  }
  // end::container-config-level-1-container-with-multi-batch-demo[]

  @Test
  void failFastForHeaderConfigMaxBatchSize() {
    final var containerValidationConfig =
        ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMember(ContainerLevel1::getBeanBatch)
            .shouldHaveMaxBatchSizeOrFailWith(Tuple.of(0, MAX_BATCH_SIZE_EXCEEDED))
            .prepare();
    final var headerBean = new ContainerLevel1(List.of(new Bean()));
    final var result = Vader.validateAndFailFastForContainer(headerBean, containerValidationConfig);
    assertThat(result).contains(MAX_BATCH_SIZE_EXCEEDED);
  }

  @Test
  void headerWithFailure() {
    final var containerValidationConfig =
        ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
            .withContainerValidatorEtrs(
                List.of(
                    headerBean -> Either.right(NONE),
                    headerBean -> Either.left(UNKNOWN_EXCEPTION),
                    headerBean -> Either.right(NONE)))
            .withBatchMember(ContainerLevel1::getBeanBatch)
            .prepare();
    final var result =
        Vader.validateAndFailFastForContainer(
            new ContainerLevel1(emptyList()), containerValidationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Test
  void getFieldNamesForBatch() {
    final var validationConfig =
        ContainerValidationConfig.<ContainerWithMultiBatch, ValidationFailure>toValidate()
            .withBatchMembers(
                List.of(ContainerWithMultiBatch::getBatch1, ContainerWithMultiBatch::getBatch2))
            .prepare();
    assertThat(validationConfig.getFieldNamesForBatch(ContainerWithMultiBatch.class))
        .containsExactly(Fields.batch1, Fields.batch2);
  }

  // tag::container-config-level-1-container-with-container-batch-demo[]
  @DisplayName(
      "Compose the validation results from a container with results from the Batch Container it contains")
  @Test
  void composeContainerValidationResults() {
    final var containerRootValidationConfig =
        ContainerValidationConfig.<ContainerRoot, ValidationFailure>toValidate()
            .withBatchMember(ContainerRoot::getContainerLevel1Batch)
            .shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_ROOT_LEVEL))
            .prepare();
    final var containerValidationConfig =
        ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMember(ContainerLevel1::getBeanBatch)
            .shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
            .prepare();

    final var beanBatch = List.of(new Bean());
    final var container2Batch =
        List.of(new ContainerLevel1(beanBatch), new ContainerLevel1(emptyList()));
    final var container1 = new ContainerRoot(container2Batch);

    final var result =
        Vader.validateAndFailFastForContainer(container1, containerRootValidationConfig)
            .or(
                () ->
                    VaderBatch.validateAndFailFastForContainer(
                        container2Batch, containerValidationConfig));

    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET_LEVEL_1);
  }
  // end::container-config-level-1-container-with-container-batch-demo[]

  @Value
  private static class Bean1 {}

  @Value
  private static class Bean2 {}

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  // tag::container-config-level-1-container-with-multi-batch[]
  public static class ContainerWithMultiBatch {
    List<Bean1> batch1;
    List<Bean2> batch2;
  }
  // end::container-config-level-1-container-with-multi-batch[]

  @Value
  // tag::container-config-level-1-container-with-container-batch[]
  private static class Bean {}
  // end::container-config-level-1-container-with-container-batch[]

  @Value
  // tag::container-config-level-1-container-with-container-batch[]
  private static class ContainerLevel1 {
    List<Bean> beanBatch;
  }
  // end::container-config-level-1-container-with-container-batch[]

  @Value
  // tag::container-config-level-1-container-with-container-batch[]
  public static class ContainerRoot {
    List<ContainerLevel1> containerLevel1Batch;
  }
  // end::container-config-level-1-container-with-container-batch[]

  @Value
  private static class ContainerWithPair {
    int id;
    List<Bean> beanBatch;
  }
}
