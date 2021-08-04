package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.MAX_BATCH_SIZE_EXCEEDED_LEVEL_2;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_LEVEL_0;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_LEVEL_1;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_LEVEL_2;
import static consumer.failure.ValidationFailure.NONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.revcloud.vader.runner.Runner.validateAndFailFastForContainer;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import java.util.List;
import kotlin.jvm.functions.Function1;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContainerValidationConfigWith2LevelsTest {

  // tag::container-config-level-2-demo[]
  @DisplayName(
      "Container with 2 levels: (Container1Root -> Container2 -> Container3) + Container with next 1 level: (Container2 -> Container3)")
  @Test
  void containerValidationConfigWith2Levels1() {
    final var container1RootValidationConfig =
        ContainerValidationConfigWith2Levels
            .<ContainerRoot, ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMapper(ContainerRoot::getContainerLevel1Batch)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_LEVEL_0))
            .withContainerLevel1ValidationConfig(
                ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
                    .withBatchMapper(ContainerLevel1::getContainerLevel2Batch)
                    .shouldHaveMinBatchSize(Tuple.of(3, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
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

    final var beanBatch1 = List.of(new Bean());
    final var beanBatch2 = List.of(new Bean());
    final var header3Batch1 =
        List.of(new ContainerLevel2(beanBatch1), new ContainerLevel2(beanBatch2));
    final var beanBatch3 = List.of(new Bean());
    final var beanBatch4 = List.of(new Bean());
    final var header3Batch2 =
        List.of(new ContainerLevel2(beanBatch3), new ContainerLevel2(beanBatch4));
    final var header2Batch =
        List.of(new ContainerLevel1(header3Batch1), new ContainerLevel1(header3Batch2));
    final var header1Root = new ContainerRoot(header2Batch);

    final var result =
        Runner.validateAndFailFastForContainer(
                header1Root, container1RootValidationConfig, throwableMapper)
            .or(
                () ->
                    Runner.validateAndFailFastForContainer(
                        header2Batch, container2ValidationConfig, throwableMapper));

    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET_LEVEL_1);
  }
  // end::container-config-level-2-demo[]

  @DisplayName(
      "Container with 2 levels: (Container1Root -> Container2 -> Container3) + Container with next 2 levels: (Container2 -> Container3 -> Bean)")
  @Test
  void containerValidationConfigWith2Levels2() {
    final var containerRootValidationConfig =
        ContainerValidationConfigWith2Levels
            .<ContainerRoot, ContainerLevel1, ValidationFailure>toValidate()
            .withBatchMapper(ContainerRoot::getContainerLevel1Batch)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_LEVEL_0))
            .withContainerLevel1ValidationConfig(
                ContainerValidationConfig.<ContainerLevel1, ValidationFailure>toValidate()
                    .withBatchMapper(ContainerLevel1::getContainerLevel2Batch)
                    .shouldHaveMinBatchSize(Tuple.of(3, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
                    .prepare())
            .prepare();
    final var container1ValidationConfig =
        ContainerValidationConfigWith2Levels
            .<ContainerLevel1, ContainerLevel2, ValidationFailure>toValidate()
            .withBatchMapper(ContainerLevel1::getContainerLevel2Batch)
            .shouldHaveMinBatchSize(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_2))
            .withContainerLevel1ValidationConfig(
                ContainerValidationConfig.<ContainerLevel2, ValidationFailure>toValidate()
                    .withBatchMapper(ContainerLevel2::getBeanBatch)
                    .shouldHaveMaxBatchSize(Tuple.of(3, MAX_BATCH_SIZE_EXCEEDED_LEVEL_2))
                    .withContainerValidator(ignore -> NONE, NONE)
                    .prepare())
            .prepare();
    final var throwableMapper =
        (Function1<Throwable, ValidationFailure>)
            ValidationFailure::getValidationFailureForException;

    final var beanBatch1 = List.of(new Bean());
    final var beanBatch2 = List.of(new Bean());
    final var header3Batch1 =
        List.of(new ContainerLevel2(beanBatch1), new ContainerLevel2(beanBatch2));
    final var beanBatch3 = List.of(new Bean());
    final var beanBatch4 = List.of(new Bean());
    final var header3Batch2 =
        List.of(new ContainerLevel2(beanBatch3), new ContainerLevel2(beanBatch4));
    final var header2Batch =
        List.of(new ContainerLevel1(header3Batch1), new ContainerLevel1(header3Batch2));
    final var header1Root = new ContainerRoot(header2Batch);

    final var result =
        Runner.validateAndFailFastForContainer(
                header1Root, containerRootValidationConfig, throwableMapper)
            .or(
                () ->
                    validateAndFailFastForContainer(
                        header2Batch, container1ValidationConfig, throwableMapper));

    assertThat(result).contains(MAX_BATCH_SIZE_EXCEEDED_LEVEL_2);
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
}
