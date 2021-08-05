package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.MAX_BATCH_SIZE_EXCEEDED;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_LEVEL_0;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_LEVEL_1;
import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import io.vavr.control.Either;
import java.util.List;
import kotlin.jvm.functions.Function1;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.ContainerValidationConfigTest.ContainerWithMultiBatch.Fields;

class ContainerValidationConfigTest {

  @Test
  void failFastForHeaderConfigWithValidators() {
    final var containerValidationConfig =
        ContainerValidationConfig.<Container1, ValidationFailure>toValidate()
            .withBatchMapper(Container1::getBeanBatch)
            .withContainerValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
            .prepare();
    final var batch = List.of(new Bean());
    final var headerBean = new Container1(batch);
    final var result =
        Runner.validateAndFailFastForContainer(
            headerBean,
            containerValidationConfig,
            ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Test
  void failFastForHeaderConfigValidatorsCount() {
    final var containerValidationConfig =
        ContainerValidationConfig.<Container1, ValidationFailure>toValidate()
            .withBatchMapper(Container1::getBeanBatch)
            .withContainerValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
            .withContainerValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
            .withContainerValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
            .prepare();
    assertThat(containerValidationConfig.getContainerValidators()).hasSize(3);
  }

  @Test
  void failFastForHeaderConfigWithValidators2() {
    final var containerValidationConfig =
        ContainerValidationConfig.<Container1, ValidationFailure>toValidate()
            .withBatchMapper(Container1::getBeanBatch)
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();
    final var batch = List.of(new Bean());
    final var headerBean = new Container1(batch);
    final var result =
        Runner.validateAndFailFastForContainer(
            headerBean,
            containerValidationConfig,
            ValidationFailure::getValidationFailureForException);
    assertThat(result).isEmpty();
  }

  @Test
  void failFastForHeaderConfigMinBatchSize() {
    final var containerValidationConfig =
        ContainerValidationConfig.<Container1, ValidationFailure>toValidate()
            .withBatchMapper(Container1::getBeanBatch)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_LEVEL_0))
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();
    final var headerBean = new Container1(emptyList());
    final var result =
        Runner.validateAndFailFastForContainer(
            headerBean,
            containerValidationConfig,
            ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET_LEVEL_0);
  }

  // tag::container-config-level-1-container-with-multi-batch-demo[]
  @Test
  void failFastForHeaderConfigMinBatchSizeForMultiBatch() {
    final var containerValidationConfig =
        ContainerValidationConfig.<ContainerWithMultiBatch, ValidationFailure>toValidate()
            .withBatchMappers(
                List.of(ContainerWithMultiBatch::getBatch1, ContainerWithMultiBatch::getBatch2))
            .shouldHaveMinBatchSize(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_LEVEL_0))
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();
    final var headerBean = new ContainerWithMultiBatch(emptyList(), List.of(new Bean2()));
    final var result =
        Runner.validateAndFailFastForContainer(
            headerBean,
            containerValidationConfig,
            ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET_LEVEL_0);
  }
  // end::container-config-level-1-container-with-multi-batch-demo[]

  @Test
  void failFastForHeaderConfigMaxBatchSize() {
    final var containerValidationConfig =
        ContainerValidationConfig.<Container1, ValidationFailure>toValidate()
            .withBatchMapper(Container1::getBeanBatch)
            .shouldHaveMaxBatchSize(Tuple.of(0, MAX_BATCH_SIZE_EXCEEDED))
            .prepare();
    final var headerBean = new Container1(List.of(new Bean()));
    final var result =
        Runner.validateAndFailFastForContainer(
            headerBean,
            containerValidationConfig,
            ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(MAX_BATCH_SIZE_EXCEEDED);
  }

  @Test
  void headerWithFailure() {
    final var containerValidationConfig =
        ContainerValidationConfig.<Container1, ValidationFailure>toValidate()
            .withContainerValidatorEtrs(
                List.of(
                    headerBean -> Either.right(NONE),
                    headerBean -> Either.left(UNKNOWN_EXCEPTION),
                    headerBean -> Either.right(NONE)))
            .withBatchMapper(Container1::getBeanBatch)
            .prepare();
    final var result =
        Runner.validateAndFailFastForContainer(
            new Container1(emptyList()),
            containerValidationConfig,
            ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Test
  void getFieldNamesForBatch() {
    final var validationConfig =
        ContainerValidationConfig.<ContainerWithMultiBatch, ValidationFailure>toValidate()
            .withBatchMappers(
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
            .withBatchMapper(ContainerRoot::getContainer1Batch)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_LEVEL_0))
            .prepare();
    final var containerValidationConfig =
        ContainerValidationConfig.<Container1, ValidationFailure>toValidate()
            .withBatchMapper(Container1::getBeanBatch)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_LEVEL_1))
            .prepare();
    final var throwableMapper =
        (Function1<Throwable, ValidationFailure>)
            ValidationFailure::getValidationFailureForException;

    final var beanBatch = List.of(new Bean());
    final var container2Batch = List.of(new Container1(beanBatch), new Container1(emptyList()));
    final var container1 = new ContainerRoot(container2Batch);

    final var result =
        Runner.validateAndFailFastForContainer(
                container1, containerRootValidationConfig, throwableMapper)
            .or(
                () ->
                    Runner.validateAndFailFastForContainer(
                        container2Batch, containerValidationConfig, throwableMapper));

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
  private static class Container1 {
    List<Bean> beanBatch;
  }
  // end::container-config-level-1-container-with-container-batch[]

  @Value
  // tag::container-config-level-1-container-with-container-batch[]
  public static class ContainerRoot {
    List<Container1> container1Batch;
  }
  // end::container-config-level-1-container-with-container-batch[]
}
