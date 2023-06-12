/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.execution.config.nested;

import static com.salesforce.vador.execution.config.nested.ContainerValidationConfigWith2LevelsTest.ContainerLevel1WithMultiBatch.Fields.beanBatch;
import static com.salesforce.vador.execution.config.nested.ContainerValidationConfigWith2LevelsTest.ContainerLevel1WithMultiBatch.Fields.containerLevel2Batch;
import static com.salesforce.vador.execution.config.nested.ContainerValidationConfigWith2LevelsTest.ContainerRootWithMultiContainerBatch.Fields.containerLevel1Batch1;
import static com.salesforce.vador.execution.config.nested.ContainerValidationConfigWith2LevelsTest.ContainerRootWithMultiContainerBatch.Fields.containerLevel1Batch2;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
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
            .<ContainerRoot, ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMember(ContainerRoot::getContainerLevel1Batch)
            .shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_ROOT_LEVEL))
            .withScopeOf1LevelDeep(
                ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
                    .withBatchMember(ContainerLevel1::getContainerLevel2Batch)
                    .shouldHaveMinBatchSizeOrFailWith(Tuple.of(5, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
                    .prepare())
            .prepare();
    final var containerLevel1ValidationConfig =
        ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMember(ContainerLevel1::getContainerLevel2Batch)
            .shouldHaveMinBatchSizeOrFailWith(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_2))
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();

    // level-3
    final var beanBatch1 = List.of(new Bean());
    final var beanBatch2 = List.of(new Bean());
    final var beanBatch3 = List.of(new Bean());
    final var beanBatch4 = List.of(new Bean());
    // level-2
    final var containerLevel2Batch1 =
        List.of(new ContainerLevel2(11, beanBatch1), new ContainerLevel2(12, beanBatch2));
    final var containerLevel2Batch2 =
        List.of(new ContainerLevel2(21, beanBatch3), new ContainerLevel2(22, beanBatch4));
    // level-1
    final var containerLevel1Batch =
        List.of(
            new ContainerLevel1(1, containerLevel2Batch1),
            new ContainerLevel1(2, containerLevel2Batch2));
    // root-level
    final var containerRoot = new ContainerRoot(containerLevel1Batch);

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
            .<ContainerRoot, ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMember(ContainerRoot::getContainerLevel1Batch)
            .shouldHaveMinBatchSizeOrFailWith(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_ROOT_LEVEL))
            .withScopeOf1LevelDeep(
                ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
                    .withBatchMember(ContainerLevel1::getContainerLevel2Batch)
                    .shouldHaveMinBatchSizeOrFailWith(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
                    .prepare())
            .prepare();
    final var containerLevel1ValidationConfigWithScopeOf1LevelDeep =
        ContainerValidationConfigWith2Levels
            .<ContainerLevel1, ContainerLevel2, ValidationFailure>toValidate()
            .withBatchMember(ContainerLevel1::getContainerLevel2Batch)
            .shouldHaveMinBatchSizeOrFailWith(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_2))
            .withScopeOf1LevelDeep(
                ContainerValidationConfig.<ContainerLevel2, ValidationFailure>toValidate()
                    .withBatchMember(ContainerLevel2::getBeanBatch)
                    .shouldHaveMaxBatchSizeOrFailWith(Tuple.of(3, MAX_BATCH_SIZE_EXCEEDED_LEVEL_2))
                    .prepare())
            .prepare();

    // level-3
    final var beanBatch1 = List.of(new Bean(), new Bean());
    final var beanBatch2 = List.of(new Bean(), new Bean());
    final var beanBatch3 = List.of(new Bean());
    final var beanBatch4 = List.of(new Bean());
    // level-2
    final var level2ContainerBatch1 =
        List.of(new ContainerLevel2(11, beanBatch1), new ContainerLevel2(12, beanBatch2));
    final var level2ContainerBatch2 =
        List.of(new ContainerLevel2(21, beanBatch3), new ContainerLevel2(22, beanBatch4));
    // level-1
    final var level1ContainerBatch =
        List.of(
            new ContainerLevel1(1, level2ContainerBatch1),
            new ContainerLevel1(2, level2ContainerBatch2));
    // root
    final var rootContainer = new ContainerRoot(level1ContainerBatch);

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
            .<ContainerLevel1, ContainerLevel2, ValidationFailure>toValidate()
            .withBatchMember(ContainerLevel1::getContainerLevel2Batch)
            .shouldHaveMinBatchSizeOrFailWith(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_2))
            .withScopeOf1LevelDeep(
                ContainerValidationConfig.<ContainerLevel2, ValidationFailure>toValidate()
                    .withBatchMember(ContainerLevel2::getBeanBatch)
                    .shouldHaveMaxBatchSizeOrFailWith(Tuple.of(3, MAX_BATCH_SIZE_EXCEEDED_LEVEL_2))
                    .prepare())
            .prepare();

    // level-3
    final var beanBatch1 = List.of(new Bean(), new Bean());
    final var beanBatch2 = List.of(new Bean(), new Bean());
    final var beanBatch3 = List.of(new Bean());
    final var beanBatch4 = List.of(new Bean());
    // level-2
    final var level2ContainerBatch1 =
        List.of(new ContainerLevel2(11, beanBatch1), new ContainerLevel2(12, beanBatch2));
    final var level2ContainerBatch2 =
        List.of(new ContainerLevel2(21, beanBatch3), new ContainerLevel2(22, beanBatch4));
    // level-1
    final var level1ContainerBatch =
        List.of(
            new ContainerLevel1(1, level2ContainerBatch1),
            new ContainerLevel1(2, level2ContainerBatch2));

    final var result =
        VadorBatch.validateAndFailFastForContainer(
            level1ContainerBatch,
            ContainerLevel1::getId,
            containerLevel1ValidationConfigWithScopeOf1LevelDeep);

    assertThat(result).contains(Tuple.of(1, MAX_BATCH_SIZE_EXCEEDED_LEVEL_2));
  }

  @Test
  void getFieldNamesForBatch() {
    final var validationConfig =
        ContainerValidationConfigWith2Levels
            .<ContainerRootWithMultiContainerBatch, ContainerLevel1WithMultiBatch,
                ValidationFailure>
                toValidate()
            .withBatchMembers(
                List.of(
                    ContainerRootWithMultiContainerBatch::getContainerLevel1Batch1,
                    ContainerRootWithMultiContainerBatch::getContainerLevel1Batch2))
            .withScopeOf1LevelDeep(
                ContainerValidationConfig
                    .<ContainerLevel1WithMultiBatch, ValidationFailure>toValidate()
                    .withBatchMembers(
                        List.of(
                            ContainerLevel1WithMultiBatch::getContainerLevel2Batch,
                            ContainerLevel1WithMultiBatch::getBeanBatch))
                    .shouldHaveMinBatchSizeOrFailWith(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
                    .prepare())
            .prepare();
    assertThat(
            validationConfig.getFieldNamesForBatchRootLevel(
                ContainerRootWithMultiContainerBatch.class))
        .containsExactly(containerLevel1Batch1, containerLevel1Batch2);
    assertThat(validationConfig.getFieldNamesForBatchLevel1(ContainerLevel1WithMultiBatch.class))
        .containsExactly(containerLevel2Batch, beanBatch);
  }

  @Value
  // tag::container-config-level-2[]
  private static class Bean {}

  // end::container-config-level-2[]

  @Value
  // tag::container-config-level-2[]
  private static class ContainerLevel2 {
    int id;
    List<Bean> beanBatch;
  }

  // end::container-config-level-2[]

  @Value
  // tag::container-config-level-2[]
  private static class ContainerLevel1 {
    int id;
    List<ContainerLevel2> containerLevel2Batch;
  }

  // end::container-config-level-2[]

  @Value
  // tag::container-config-level-2[]
  public static class ContainerRoot {
    List<ContainerLevel1> containerLevel1Batch;
  }

  // end::container-config-level-2[]

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  public static class ContainerLevel1WithMultiBatch {
    List<ContainerLevel2> containerLevel2Batch;
    List<Bean> beanBatch;
  }

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  public static class ContainerRootWithMultiContainerBatch {
    List<ContainerLevel1WithMultiBatch> containerLevel1Batch1;
    List<ContainerLevel1WithMultiBatch> containerLevel1Batch2;
  }
}
