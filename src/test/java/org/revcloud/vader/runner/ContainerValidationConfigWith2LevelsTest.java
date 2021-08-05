package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.MAX_BATCH_SIZE_EXCEEDED_LEVEL_2;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_LEVEL_0;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_LEVEL_1;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_LEVEL_2;
import static consumer.failure.ValidationFailure.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.revcloud.vader.runner.ContainerValidationConfigWith2LevelsTest.ContainerLevel1WithMultiBatch.Fields.beanBatch;
import static org.revcloud.vader.runner.ContainerValidationConfigWith2LevelsTest.ContainerLevel1WithMultiBatch.Fields.containerLevel2Batch;
import static org.revcloud.vader.runner.ContainerValidationConfigWith2LevelsTest.ContainerRootWithMultiContainerBatch.Fields.containerLevel1Batch1;
import static org.revcloud.vader.runner.ContainerValidationConfigWith2LevelsTest.ContainerRootWithMultiContainerBatch.Fields.containerLevel1Batch2;
import static org.revcloud.vader.runner.Runner.validateAndFailFastForContainer;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import java.util.List;
import kotlin.jvm.functions.Function1;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContainerValidationConfigWith2LevelsTest {

  // tag::container-config-level-2-demo[]
  @DisplayName(
      "Container with 2 levels: (ContainerRoot -> Container1 -> Container2) + Container with next 1 level: (Container1 -> Container2)")
  @Test
  void containerValidationConfigWith2Levels1() {
    final var container1RootValidationConfig =
        ContainerValidationConfigWith2Levels
            .<ContainerRoot, ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMapper(ContainerRoot::getContainerLevel1Batch)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_LEVEL_0))
            .withScopeOf1LevelDeep(
                ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
                    .withBatchMapper(ContainerLevel1::getContainerLevel2Batch)
                    .shouldHaveMinBatchSize(Tuple.of(5, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
                    .prepare())
            .prepare();
    final var container2ValidationConfig =
        ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMapper(ContainerLevel1::getContainerLevel2Batch)
            .shouldHaveMinBatchSize(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_2))
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();

    final var throwableMapper =
        (Function1<Throwable, ValidationFailure>)
            ValidationFailure::getValidationFailureForException;

    // level-3
    final var beanBatch1 = List.of(new Bean());
    final var beanBatch2 = List.of(new Bean());
    final var beanBatch3 = List.of(new Bean());
    final var beanBatch4 = List.of(new Bean());
    // level-2
    final var containerLevel2Batch1 =
        List.of(new ContainerLevel2(beanBatch1), new ContainerLevel2(beanBatch2));
    final var containerLevel2Batch2 =
        List.of(new ContainerLevel2(beanBatch3), new ContainerLevel2(beanBatch4));
    // level-1
    final var containerLevel1Batch =
        List.of(
            new ContainerLevel1(containerLevel2Batch1), new ContainerLevel1(containerLevel2Batch2));
    // root-level
    final var containerRoot = new ContainerRoot(containerLevel1Batch);

    final var result =
        Runner.validateAndFailFastForContainer(
                containerRoot, container1RootValidationConfig, throwableMapper)
            .or(
                () ->
                    Runner.validateAndFailFastForContainer(
                        containerLevel1Batch, container2ValidationConfig, throwableMapper));

    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET_LEVEL_1);
  }
  // end::container-config-level-2-demo[]

  @DisplayName(
      "Container with 2 levels: (ContainerRoot -> Container2 -> Container3) + Container with next 2 levels: (Container2 -> Container3 -> Bean)")
  @Test
  void containerValidationConfigWith2Levels2() {
    final var containerRootValidationConfig =
        ContainerValidationConfigWith2Levels
            .<ContainerRoot, ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMapper(ContainerRoot::getContainerLevel1Batch)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_LEVEL_0))
            .withScopeOf1LevelDeep(
                ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
                    .withBatchMapper(ContainerLevel1::getContainerLevel2Batch)
                    .shouldHaveMinBatchSize(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
                    .prepare())
            .prepare();
    final var container1ValidationConfig =
        ContainerValidationConfigWith2Levels
            .<ContainerLevel1, ContainerLevel2, ValidationFailure>toValidate()
            .withBatchMapper(ContainerLevel1::getContainerLevel2Batch)
            .shouldHaveMinBatchSize(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_2))
            .withScopeOf1LevelDeep(
                ContainerValidationConfig.<ContainerLevel2, ValidationFailure>toValidate()
                    .withBatchMapper(ContainerLevel2::getBeanBatch)
                    .shouldHaveMaxBatchSize(Tuple.of(3, MAX_BATCH_SIZE_EXCEEDED_LEVEL_2))
                    .prepare())
            .prepare();
    final var throwableMapper =
        (Function1<Throwable, ValidationFailure>)
            ValidationFailure::getValidationFailureForException;

    // level-3
    final var beanBatch1 = List.of(new Bean(), new Bean());
    final var beanBatch2 = List.of(new Bean(), new Bean());
    final var beanBatch3 = List.of(new Bean());
    final var beanBatch4 = List.of(new Bean());
    // level-2
    final var containerLevel2Batch1 =
        List.of(new ContainerLevel2(beanBatch1), new ContainerLevel2(beanBatch2));
    final var containerLevel2Batch2 =
        List.of(new ContainerLevel2(beanBatch3), new ContainerLevel2(beanBatch4));
    // level-1
    final var containerLevel1Batch =
        List.of(
            new ContainerLevel1(containerLevel2Batch1), new ContainerLevel1(containerLevel2Batch2));
    // root
    final var header1Root = new ContainerRoot(containerLevel1Batch);

    final var result =
        Runner.validateAndFailFastForContainer(
                header1Root, containerRootValidationConfig, throwableMapper)
            .or(
                () ->
                    validateAndFailFastForContainer(
                        containerLevel1Batch, container1ValidationConfig, throwableMapper));

    assertThat(result).contains(MAX_BATCH_SIZE_EXCEEDED_LEVEL_2);
  }

  @Test
  void getFieldNamesForBatch() {
    final var validationConfig =
        ContainerValidationConfigWith2Levels
            .<ContainerRootWithMultiContainerBatch, ContainerLevel1WithMultiBatch,
                ValidationFailure>
                toValidate()
            .withBatchMappers(
                List.of(
                    ContainerRootWithMultiContainerBatch::getContainerLevel1Batch1,
                    ContainerRootWithMultiContainerBatch::getContainerLevel1Batch2))
            .withScopeOf1LevelDeep(
                ContainerValidationConfig
                    .<ContainerLevel1WithMultiBatch, ValidationFailure>toValidate()
                    .withBatchMappers(
                        List.of(
                            ContainerLevel1WithMultiBatch::getContainerLevel2Batch,
                            ContainerLevel1WithMultiBatch::getBeanBatch))
                    .shouldHaveMinBatchSize(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
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
    List<Bean> beanBatch;
  }
  // end::container-config-level-2[]

  @Value
  // tag::container-config-level-2[]
  private static class ContainerLevel1 {
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
  // tag::container-config-level-2[]
  public static class ContainerRootWithMultiContainerBatch {
    List<ContainerLevel1WithMultiBatch> containerLevel1Batch1;
    List<ContainerLevel1WithMultiBatch> containerLevel1Batch2;
  }
}
