package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.MAX_BATCH_SIZE_EXCEEDED;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET;
import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.revcloud.vader.runner.Runner.validateAndFailFastForHeader;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import io.vavr.control.Either;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.HeaderValidationConfigTest.HeaderBeanMultiBatch.Fields;

class HeaderValidationConfigTest {

  @Test
  void failFastForHeaderConfigWithValidators() {
    final var headerConfig =
        HeaderValidationConfig.<HeaderBean1, ValidationFailure>toValidate()
            .withBatchMapper(HeaderBean1::getBatch)
            .withHeaderValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
            .prepare();
    final var batch = List.of(new Bean1());
    final var headerBean = new HeaderBean1(batch);
    final var result =
        validateAndFailFastForHeader(
            headerBean, ValidationFailure::getValidationFailureForException, headerConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  // TODO 29/04/21 gopala.akshintala: Write display names for tests
  @Test
  void failFastForHeaderConfigWithValidators2() {
    final var headerConfig =
        HeaderValidationConfig.<HeaderBean1, ValidationFailure>toValidate()
            .withBatchMapper(HeaderBean1::getBatch)
            .withHeaderValidator(ignore -> NONE, NONE)
            .prepare();
    final var batch = List.of(new Bean1());
    final var headerBean = new HeaderBean1(batch);
    final var result =
        validateAndFailFastForHeader(
            headerBean, ValidationFailure::getValidationFailureForException, headerConfig);
    assertThat(result).isEmpty();
  }

  @Test
  void failFastForHeaderConfigMinBatchSize() {
    final var headerConfig =
        HeaderValidationConfig.<HeaderBean1, ValidationFailure>toValidate()
            .withBatchMapper(HeaderBean1::getBatch)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET))
            .withHeaderValidator(ignore -> NONE, NONE)
            .prepare();
    final var headerBean = new HeaderBean1(Collections.emptyList());
    final var result =
        validateAndFailFastForHeader(
            headerBean, ValidationFailure::getValidationFailureForException, headerConfig);
    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET);
  }

  @Test
  void failFastForHeaderConfigMinBatchSizeForMultiBatch() {
    final var headerConfig =
        HeaderValidationConfig.<HeaderBeanMultiBatch, ValidationFailure>toValidate()
            .withBatchMappers(
                List.of(HeaderBeanMultiBatch::getBatch1, HeaderBeanMultiBatch::getBatch2))
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET))
            .withHeaderValidator(ignore -> NONE, NONE)
            .prepare();
    final var headerBean =
        new HeaderBeanMultiBatch(Collections.emptyList(), Collections.emptyList());
    final var result =
        validateAndFailFastForHeader(
            headerBean, ValidationFailure::getValidationFailureForException, headerConfig);
    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET);
  }

  @Test
  void failFastForHeaderConfigMaxBatchSize() {
    final var headerConfig =
        HeaderValidationConfig.<HeaderBean1, ValidationFailure>toValidate()
            .withBatchMapper(HeaderBean1::getBatch)
            .shouldHaveMaxBatchSize(Tuple.of(0, MAX_BATCH_SIZE_EXCEEDED))
            .withHeaderValidator(ignore -> NONE, NONE)
            .prepare();
    final var headerBean = new HeaderBean1(List.of(new Bean1()));
    final var result =
        validateAndFailFastForHeader(
            headerBean, ValidationFailure::getValidationFailureForException, headerConfig);
    assertThat(result).contains(MAX_BATCH_SIZE_EXCEEDED);
  }

  @Test
  void headerWithFailure() {
    final var headerValidationConfig =
        HeaderValidationConfig.<HeaderBean1, ValidationFailure>toValidate()
            .withHeaderValidatorEtrs(
                List.of(
                    headerBean -> Either.right(NONE),
                    headerBean -> Either.left(UNKNOWN_EXCEPTION),
                    headerBean -> Either.right(NONE)))
            .withBatchMapper(HeaderBean1::getBatch)
            .prepare();
    final var result =
        validateAndFailFastForHeader(
            new HeaderBean1(Collections.emptyList()),
            ValidationFailure::getValidationFailureForException,
            headerValidationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Test
  void getFieldNamesForBatch() {
    final var validationConfig =
        HeaderValidationConfig.<HeaderBeanMultiBatch, ValidationFailure>toValidate()
            .withBatchMappers(
                List.of(HeaderBeanMultiBatch::getBatch1, HeaderBeanMultiBatch::getBatch2))
            .prepare();
    assertThat(validationConfig.getFieldNamesForBatch(HeaderBeanMultiBatch.class))
        .containsExactly(Fields.batch1, Fields.batch2);
  }

  @Value
  private static class Bean1 {}

  @Value
  private static class Bean2 {}

  @Value
  private static class HeaderBean1 {
    List<Bean1> batch;
  }

  @Value
  private static class HeaderBean2 {
    List<Bean1> batch;
  }

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  public static class HeaderBeanMultiBatch {
    List<Bean1> batch1;
    List<Bean2> batch2;
  }
  
}
