package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.MAX_NESTED_BATCH_SIZE_EXCEEDED_2;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_1;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_2;
import static consumer.failure.ValidationFailure.MIN_NESTED_BATCH_SIZE_NOT_MET_1;
import static consumer.failure.ValidationFailure.NONE;
import static org.assertj.core.api.Assertions.assertThat;
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

class ContainerValidationConfigLevel2Test {

  @DisplayName("Min batch size for nested batch: Depth - 1")
  @Test
  void nestedBatchHeader1() {
    final var header1RootValidationConfig =
        ContainerValidationConfigLevel2.<Container1Root, Container2, ValidationFailure>toValidate()
            .withBatchMapper(Container1Root::getContainer2)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_1))
            .withContainerLevel2ValidationConfig(
                ContainerValidationConfig.<Container2, ValidationFailure>toValidate()
                    .withBatchMapper(Container2::getContainer3Batches)
                    .shouldHaveMinBatchSize(Tuple.of(3, MIN_NESTED_BATCH_SIZE_NOT_MET_1))
                    .prepare())
            .prepare();
    final var header2ValidationConfig =
        ContainerValidationConfig.<Container2, ValidationFailure>toValidate()
            .withBatchMapper(Container2::getContainer3Batches)
            .shouldHaveMinBatchSize(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_2))
            .withContainerValidator(ignore -> NONE, NONE)
            .prepare();

    final var throwableMapper =
        (Function1<Throwable, ValidationFailure>)
            ValidationFailure::getValidationFailureForException;

    final var beanBatch1 = List.of(new Bean());
    final var beanBatch2 = List.of(new Bean());
    final var header3Batch1 = List.of(new Container3(beanBatch1), new Container3(beanBatch2));
    final var beanBatch3 = List.of(new Bean());
    final var beanBatch4 = List.of(new Bean());
    final var header3Batch2 = List.of(new Container3(beanBatch3), new Container3(beanBatch4));
    final var header2Batch = List.of(new Container2(header3Batch1), new Container2(header3Batch2));
    final var header1Root = new Container1Root(header2Batch);

    final var result =
        Runner.validateAndFailFastForContainer(
                header1Root, header1RootValidationConfig, throwableMapper)
            .or(
                () ->
                    Runner.validateAndFailFastForContainer(
                        header2Batch, header2ValidationConfig, throwableMapper));

    assertThat(result).contains(MIN_NESTED_BATCH_SIZE_NOT_MET_1);
  }

  // tag::container-config-level-2-demo[]
  @DisplayName("Min batch size for nested batch: Depth - 2")
  @Test
  void nestedBatchHeader2() {
    final var header1RootValidationConfig =
        ContainerValidationConfigLevel2.<Container1Root, Container2, ValidationFailure>toValidate()
            .withBatchMapper(Container1Root::getContainer2)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_1))
            .withContainerLevel2ValidationConfig(
                ContainerValidationConfig.<Container2, ValidationFailure>toValidate()
                    .withBatchMapper(Container2::getContainer3Batches)
                    .shouldHaveMinBatchSize(Tuple.of(3, MIN_NESTED_BATCH_SIZE_NOT_MET_1))
                    .prepare())
            .prepare();
    final var header2ValidationConfig =
        ContainerValidationConfigLevel2.<Container2, Container3, ValidationFailure>toValidate()
            .withBatchMapper(Container2::getContainer3Batches)
            .shouldHaveMinBatchSize(Tuple.of(2, MIN_BATCH_SIZE_NOT_MET_2))
            .withContainerLevel2ValidationConfig(
                ContainerValidationConfig.<Container3, ValidationFailure>toValidate()
                    .withBatchMapper(Container3::getBeanBatch)
                    .shouldHaveMaxBatchSize(Tuple.of(3, MAX_NESTED_BATCH_SIZE_EXCEEDED_2))
                    .withContainerValidator(ignore -> NONE, NONE)
                    .prepare())
            .prepare();
    final var throwableMapper =
        (Function1<Throwable, ValidationFailure>)
            ValidationFailure::getValidationFailureForException;

    final var beanBatch1 = List.of(new Bean());
    final var beanBatch2 = List.of(new Bean());
    final var header3Batch1 = List.of(new Container3(beanBatch1), new Container3(beanBatch2));
    final var beanBatch3 = List.of(new Bean());
    final var beanBatch4 = List.of(new Bean());
    final var header3Batch2 = List.of(new Container3(beanBatch3), new Container3(beanBatch4));
    final var header2Batch = List.of(new Container2(header3Batch1), new Container2(header3Batch2));
    final var header1Root = new Container1Root(header2Batch);

    final var result =
        Runner.validateAndFailFastForContainer(
                header1Root, header1RootValidationConfig, throwableMapper)
            .or(
                () ->
                    validateAndFailFastForContainer(
                        header2Batch, header2ValidationConfig, throwableMapper));

    assertThat(result).contains(MAX_NESTED_BATCH_SIZE_EXCEEDED_2);
  }
  // end::container-config-level-2-demo[]

  @Value
  // tag::container-config-level-2[]
  private static class Bean {}
  // end::container-config-level-2[]

  @Value
  // tag::container-config-level-2[]
  private static class Container3 {
    List<Bean> beanBatch;
  }
  // end::container-config-level-2[]

  @Value
  // tag::container-config-level-2[]
  private static class Container2 {
    List<Container3> container3Batches;
  }
  // end::container-config-level-2[]

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  // tag::container-config-level-2[]
  public static class Container1Root {
    List<Container2> container2;
  }
  // end::container-config-level-2[]
}
