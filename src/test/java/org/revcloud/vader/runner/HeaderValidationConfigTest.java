package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.MAX_BATCH_SIZE_EXCEEDED;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET_1;
import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.revcloud.vader.runner.Runner.validateAndFailFastForHeader;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import io.vavr.control.Either;
import java.util.Collections;
import java.util.List;
import kotlin.jvm.functions.Function1;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.HeaderValidationConfigTest.HeaderBeanMultiBatch.Fields;

class HeaderValidationConfigTest {

  @Test
  void failFastForHeaderConfigWithValidators() {
    final var headerConfig =
        HeaderValidationConfig.<Header2, ValidationFailure>toValidate()
            .withBatchMapper(Header2::getBeanBatch)
            .withHeaderValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
            .prepare();
    final var batch = List.of(new Bean());
    final var headerBean = new Header2(batch);
    final var result =
        validateAndFailFastForHeader(
            headerBean, headerConfig, ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  // TODO 29/04/21 gopala.akshintala: Write display names for tests
  @Test
  void failFastForHeaderConfigWithValidators2() {
    final var headerConfig =
        HeaderValidationConfig.<Header2, ValidationFailure>toValidate()
            .withBatchMapper(Header2::getBeanBatch)
            .withHeaderValidator(ignore -> NONE, NONE)
            .prepare();
    final var batch = List.of(new Bean());
    final var headerBean = new Header2(batch);
    final var result =
        validateAndFailFastForHeader(
            headerBean, headerConfig, ValidationFailure::getValidationFailureForException);
    assertThat(result).isEmpty();
  }

  @Test
  void failFastForHeaderConfigMinBatchSize() {
    final var headerConfig =
        HeaderValidationConfig.<Header2, ValidationFailure>toValidate()
            .withBatchMapper(Header2::getBeanBatch)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_1))
            .withHeaderValidator(ignore -> NONE, NONE)
            .prepare();
    final var headerBean = new Header2(Collections.emptyList());
    final var result =
        validateAndFailFastForHeader(
            headerBean, headerConfig, ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET_1);
  }

  @Test
  void failFastForHeaderConfigMinBatchSizeForMultiBatch() {
    final var headerConfig =
        HeaderValidationConfig.<HeaderBeanMultiBatch, ValidationFailure>toValidate()
            .withBatchMappers(
                List.of(HeaderBeanMultiBatch::getBatch1, HeaderBeanMultiBatch::getBatch2))
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_1))
            .withHeaderValidator(ignore -> NONE, NONE)
            .prepare();
    final var headerBean =
        new HeaderBeanMultiBatch(Collections.emptyList(), Collections.emptyList());
    final var result =
        validateAndFailFastForHeader(
            headerBean, headerConfig, ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET_1);
  }

  @Test
  void failFastForHeaderConfigMaxBatchSize() {
    final var headerConfig =
        HeaderValidationConfig.<Header2, ValidationFailure>toValidate()
            .withBatchMapper(Header2::getBeanBatch)
            .shouldHaveMaxBatchSize(Tuple.of(0, MAX_BATCH_SIZE_EXCEEDED))
            .withHeaderValidator(ignore -> NONE, NONE)
            .prepare();
    final var headerBean = new Header2(List.of(new Bean()));
    final var result =
        validateAndFailFastForHeader(
            headerBean, headerConfig, ValidationFailure::getValidationFailureForException);
    assertThat(result).contains(MAX_BATCH_SIZE_EXCEEDED);
  }

  @Test
  void headerWithFailure() {
    final var headerValidationConfig =
        HeaderValidationConfig.<Header2, ValidationFailure>toValidate()
            .withHeaderValidatorEtrs(
                List.of(
                    headerBean -> Either.right(NONE),
                    headerBean -> Either.left(UNKNOWN_EXCEPTION),
                    headerBean -> Either.right(NONE)))
            .withBatchMapper(Header2::getBeanBatch)
            .prepare();
    final var result =
        validateAndFailFastForHeader(
            new Header2(Collections.emptyList()),
            headerValidationConfig,
            ValidationFailure::getValidationFailureForException);
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

  @DisplayName(
      "Nested batch with Failure in deepest level `Header1Nested -> List<Header2> -> List<Bean1> (^^^UNKNOWN_EXCEPTION)`")
  @Test
  void nestedBatchHeader1() {
    final var header1NestedValidationConfig =
        HeaderValidationConfig.<Header1Root, ValidationFailure>toValidate()
            .withBatchMapper(Header1Root::getHeader2)
            .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET_1))
            .prepare();
    final var header2ValidationConfig1 =
        HeaderValidationConfig.<Header2, ValidationFailure>toValidate()
            .withBatchMapper(Header2::getBeanBatch)
            .withHeaderValidator(ignore -> NONE, NONE)
            .prepare();
    final var strBatch1ValidationConfig =
        HeaderValidationConfig.<String, ValidationFailure>toValidate()
            .withHeaderValidator(ignore -> NONE, NONE)
            .prepare();
    final var beanBatch2ValidationConfig =
        HeaderValidationConfig.<Bean, ValidationFailure>toValidate()
            .withHeaderValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
            .prepare();
    final var throwableMapper =
        (Function1<Throwable, ValidationFailure>)
            ValidationFailure::getValidationFailureForException;

    final var beanBatch2 = List.of(new Bean());
    final var header2Batch = List.of(new Header2(beanBatch2));
    final List<String> strBatch1 = Collections.emptyList();
    final var header1Nested = new Header1Root(header2Batch, strBatch1);

    final var result =
        validateAndFailFastForHeader(header1Nested, header1NestedValidationConfig, throwableMapper)
            .or(
                () ->
                    validateAndFailFastForHeader(
                            header2Batch, header2ValidationConfig1, throwableMapper)
                        .or(
                            () ->
                                // TODO 27/07/21 gopala.akshintala: Handle multiple items in batch
                                // in a generic way
                                validateAndFailFastForHeader(
                                    beanBatch2, beanBatch2ValidationConfig, throwableMapper)))
            .or(
                () ->
                    validateAndFailFastForHeader(
                        strBatch1, strBatch1ValidationConfig, throwableMapper));

    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Value
  private static class Bean {}

  @Value
  private static class Bean1 {}

  @Value
  private static class Bean2 {}

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  public static class HeaderBeanMultiBatch {

    List<Bean1> batch1;
    List<Bean2> batch2;
  }

  @Value
  private static class Header2 {
    List<Bean> beanBatch;
  }

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  public static class Header1Root {

    List<Header2> header2;
    List<String> batch1Str;
  }
}
