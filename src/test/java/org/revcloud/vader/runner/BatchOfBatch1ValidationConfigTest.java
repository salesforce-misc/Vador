package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.INVALID_COMBO_1;
import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.revcloud.vader.matchers.AnyMatchers.anyOf;
import static org.revcloud.vader.runner.BatchRunner.validateAndFailFastForEach;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.assertj.vavr.api.VavrAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BatchOfBatch1ValidationConfigTest {

  @DisplayName("Validate Batch of (NestedBatch (Batch of Bean))")
  @Test
  void nestedBatchFailFast() {
    final var memberBatchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .withSpec(
                spec ->
                    spec._2()
                        .when(Bean::getValue)
                        .matches(is(1))
                        .then(Bean::getLabel)
                        .shouldMatch(anyOf("1", "one"))
                        .orFailWith(INVALID_COMBO_1))
            .withValidator(ignore -> UNKNOWN_EXCEPTION, NONE)
            .prepare();
    final var nestedBatchValidationConfig =
        BatchOfBatch1ValidationConfig.<BatchOfBatch, Bean, ValidationFailure>toValidate()
            .withMemberBatchValidationConfig(
                Tuple.of(BatchOfBatch::getBatch, memberBatchValidationConfig))
            .prepare();

    final var beanBatch = List.of(new Bean(1, "a"), new Bean(1, "1"));
    final var nestedBatch = List.of(new BatchOfBatch(beanBatch));
    final var results =
        validateAndFailFastForEach(
            nestedBatch,
            nestedBatchValidationConfig,
            NONE,
            ValidationFailure::getValidationFailureForException);
    assertThat(results).hasSize(1);
    final var result = results.get(0);
    VavrAssertions.assertThat(result).isLeft();
    final var failure = result.getLeft();
    VavrAssertions.assertThat(failure.getFailure())
        .containsOnRight(List.of(INVALID_COMBO_1, UNKNOWN_EXCEPTION));
  }

  @Value
  private static class Bean {
    int value;
    String label;
  }

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  public static class BatchOfBatch {
    List<Bean> batch;
  }
}
