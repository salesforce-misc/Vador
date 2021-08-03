package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.MAX_BATCH_SIZE_EXCEEDED;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_1;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_2;
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
import org.revcloud.vader.runner.ContainerValidationConfigTest.ContainerBeanMultiBatch.Fields;

class ContainerValidationConfigTest {

  @Test
  void failFastForHeaderConfigWithValidators() {
    final var headerConfig =
        ContainerValidationConfig.<Container2, ValidationFailure>toValidate()
            .withBatchMapper(Container2::getBeanBatch)
            .withContainerValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
            .prepare();
    final var batch = List.of(new Bean());
    final var headerBean = new Container2(batch);
    final var result =
        Runner.validateAndFailFastForContainer(
            headerBean, headerConfig, ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  // TODO 29/04/21 gopala.akshintala: Write display names for tests
  @Test
  void failFastForHeaderConfigWithValidators2() {
    final var headerConfig =
        ContainerValidationConfig.<Container2, ValidationFailure>toValidate()
            .withBatchMapper(Container2::getBeanBatch)
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();
    final var batch = List.of(new Bean());
    final var headerBean = new Container2(batch);
    final var result =
        Runner.validateAndFailFastForContainer(
            headerBean, headerConfig, ValidationFailure::getValidationFailureForException);
    assertThat(result).isEmpty();
  }

  @Test
  void failFastForHeaderConfigMinBatchSize() {
    final var headerConfig =
        ContainerValidationConfig.<Container2, ValidationFailure>toValidate()
            .withBatchMapper(Container2::getBeanBatch)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_1))
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();
    final var headerBean = new Container2(emptyList());
    final var result =
        Runner.validateAndFailFastForContainer(
            headerBean, headerConfig, ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET_1);
  }

  @Test
  void failFastForHeaderConfigMinBatchSizeForMultiBatch() {
    final var headerConfig =
        ContainerValidationConfig.<ContainerBeanMultiBatch, ValidationFailure>toValidate()
            .withBatchMappers(
                List.of(ContainerBeanMultiBatch::getBatch1, ContainerBeanMultiBatch::getBatch2))
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_1))
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();
    final var headerBean = new ContainerBeanMultiBatch(emptyList(), emptyList());
    final var result =
        Runner.validateAndFailFastForContainer(
            headerBean, headerConfig, ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET_1);
  }

  @Test
  void failFastForHeaderConfigMaxBatchSize() {
    final var headerConfig =
        ContainerValidationConfig.<Container2, ValidationFailure>toValidate()
            .withBatchMapper(Container2::getBeanBatch)
            .shouldHaveMaxBatchSize(Tuple.of(0, MAX_BATCH_SIZE_EXCEEDED))
            .prepare();
    final var headerBean = new Container2(List.of(new Bean()));
    final var result =
        Runner.validateAndFailFastForContainer(
            headerBean, headerConfig, ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(MAX_BATCH_SIZE_EXCEEDED);
  }

  @Test
  void headerWithFailure() {
    final var headerValidationConfig =
        ContainerValidationConfig.<Container2, ValidationFailure>toValidate()
            .withContainerValidatorEtrs(
                List.of(
                    headerBean -> Either.right(NONE),
                    headerBean -> Either.left(UNKNOWN_EXCEPTION),
                    headerBean -> Either.right(NONE)))
            .withBatchMapper(Container2::getBeanBatch)
            .prepare();
    final var result =
        Runner.validateAndFailFastForContainer(
            new Container2(emptyList()),
            headerValidationConfig,
            ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Test
  void getFieldNamesForBatch() {
    final var validationConfig =
        ContainerValidationConfig.<ContainerBeanMultiBatch, ValidationFailure>toValidate()
            .withBatchMappers(
                List.of(ContainerBeanMultiBatch::getBatch1, ContainerBeanMultiBatch::getBatch2))
            .prepare();
    assertThat(validationConfig.getFieldNamesForBatch(ContainerBeanMultiBatch.class))
        .containsExactly(Fields.batch1, Fields.batch2);
  }

  // tag::container-config-level-1-demo[]
  @DisplayName(
      "Compose the validation results from a container with results from the Batch Container it contains")
  @Test
  void composeContainerValidationResults() {
    final var container1ValidationConfig =
        ContainerValidationConfig.<Container1Root, ValidationFailure>toValidate()
            .withBatchMapper(Container1Root::getContainer2)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_1))
            .prepare();
    final var container2ValidationConfig =
        ContainerValidationConfig.<Container2, ValidationFailure>toValidate()
            .withBatchMapper(Container2::getBeanBatch)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_2))
            .prepare();
    final var throwableMapper =
        (Function1<Throwable, ValidationFailure>)
            ValidationFailure::getValidationFailureForException;

    final var beanBatch = List.of(new Bean());
    final var container2Batch = List.of(new Container2(beanBatch), new Container2(emptyList()));
    final var container1 = new Container1Root(container2Batch);

    final var result =
        Runner.validateAndFailFastForContainer(
                container1, container1ValidationConfig, throwableMapper)
            .or(
                () ->
                    Runner.validateAndFailFastForContainer(
                        container2Batch, container2ValidationConfig, throwableMapper));

    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET_2);
  }
  // end::container-config-level-1-demo[]

  @Value
  private static class Bean1 {}

  @Value
  private static class Bean2 {}

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  public static class ContainerBeanMultiBatch {
    List<Bean1> batch1;
    List<Bean2> batch2;
  }

  @Value
  // tag::container-config-level-1[]
  private static class Bean {}
  // end::container-config-level-1[]

  @Value
  // tag::container-config-level-1[]
  private static class Container2 {
    List<Bean> beanBatch;
  }
  // end::container-config-level-1[]

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  // tag::container-config-level-1[]
  public static class Container1Root {
    List<Container2> container2;
  }
  // end::container-config-level-1[]
}
