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
import lombok.Value;
import org.assertj.vavr.api.VavrAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BatchOfBatch1ValidationConfigTest {

  @DisplayName(
      "Validate a structure like batchOf(Root[batchOf(bean)]) or like `List<Root<List<Bean>>`")
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
    final var itemBatchValidationConfig =
        BatchOfBatch1ValidationConfig.<Item, Bean, ValidationFailure>toValidate()
            .withMemberBatchValidationConfig(
                Tuple.of(Item::getBeanBatch, memberBatchValidationConfig))
            .prepare();

    final var invalidBean = new Bean(1, "a");
    final var beanBatch = List.of(invalidBean, new Bean(1, "1"));
    final var itemsBatch = List.of(new Item(beanBatch));
    final var root = new Root(itemsBatch);

    final var results =
        validateAndFailFastForEach(
            root.getItemsBatch(),
            itemBatchValidationConfig,
            NONE,
            ValidationFailure::getValidationFailureForException);
    assertThat(results).hasSize(1);
    final var result = results.get(0);
    VavrAssertions.assertThat(result).isLeft();
    final var failure = result.getLeft();
    assertThat(failure.getContainerFailure()).isNull();
    assertThat(failure.getBatchMemberFailures())
        .containsExactly(INVALID_COMBO_1, UNKNOWN_EXCEPTION);
  }

  @Value
  private static class Bean {
    int value;
    String label;
  }

  @Value
  private static class Item {
    List<Bean> beanBatch;
  }

  @Value
  private static class Root {
    List<Item> itemsBatch;
  }
}
