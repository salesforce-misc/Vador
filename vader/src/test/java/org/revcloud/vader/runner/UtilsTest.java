package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.DUPLICATE_ITEM;
import static consumer.failure.ValidationFailure.DUPLICATE_ITEM_1;
import static consumer.failure.ValidationFailure.DUPLICATE_ITEM_2;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static consumer.failure.ValidationFailure.NULL_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.revcloud.vader.runner.Utils.findAndFilterInvalids;
import static org.revcloud.vader.runner.Utils.findFirstInvalid;

import com.force.swag.id.ID;
import consumer.failure.ValidationFailure;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UtilsTest {

  // tag::batch-bean-demo[]
  @DisplayName("FailForDuplicates configured. FAIL: NullValidatbles, FAIL: Duplicates")
  @Test
  void filterNullValidatablesAndFailDuplicates() {
    final List<Bean> nullValidatables = List.of(null, null);
    final var duplicateValidatables =
        List.of(
            new Bean(new ID("802xx000001ni4xAAA")),
            new Bean(new ID("802xx000001ni4x")),
            new Bean(new ID("802xx000001ni4x")));
    final var validatables =
        nullValidatables
            .appendAll(duplicateValidatables)
            .appendAll(
                List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesConfig(
                FilterDuplicatesConfig.<Bean, ValidationFailure>toValidate()
                    .findAndFilterDuplicatesWith(
                        container -> container.getId().get18CharIdIfValid())
                    .andFailDuplicatesWith(DUPLICATE_ITEM)
                    .prepare())
            .prepare();
    final var results =
        List.ofAll(
            findAndFilterInvalids(
                validatables.toJavaList(),
                NOTHING_TO_VALIDATE,
                batchValidationConfig.findAndFilterDuplicatesConfigs));

    final var failedInvalids = results.take(2);
    assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);
    final var failedDuplicates = results.drop(2).take(3);
    assertThat(failedDuplicates).allMatch(r -> r.getLeft() == DUPLICATE_ITEM);

    final var valids = results.drop(5);
    assertTrue(valids.forAll(Either::isRight));
    valids.forEachWithIndex(
        (r, i) -> assertEquals(String.valueOf(i + 1), r.get().getId().toString()));
  }
  // end::batch-bean-demo[]

  // tag::batch-bean-multikey-demo[]
  @DisplayName(
      "Multiple Filters - FailForDuplicates configured. FAIL: NullValidatbles, FAIL: Duplicates")
  @Test
  void filterNullValidatablesAndFailDuplicatesForMultipleFilters() {
    final List<MultiKeyBean> nullValidatables = List.of(null, null);
    final var duplicateValidatables =
        List.of(
            new MultiKeyBean(new ID("802xx000001ni4xAAA"), new ID("802xx000001ni5xAAA")),
            new MultiKeyBean(new ID("802xx000001ni4x"), new ID("802xx000001ni4x")),
            new MultiKeyBean(new ID("802xx000001ni5x"), new ID("802xx000001ni4xAAA")));
    final var validatables =
        nullValidatables
            .appendAll(duplicateValidatables)
            .appendAll(
                List.of(
                    new MultiKeyBean(new ID("1"), new ID("1")),
                    new MultiKeyBean(new ID("2"), new ID("2")),
                    new MultiKeyBean(new ID("3"), new ID("3"))));

    final var batchValidationConfig =
        BatchValidationConfig.<MultiKeyBean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesConfig(
                FilterDuplicatesConfig.<MultiKeyBean, ValidationFailure>toValidate()
                    .findAndFilterDuplicatesWith(
                        container ->
                            container.getId1() == null
                                ? null
                                : container.getId1().get18CharIdIfValid())
                    .andFailDuplicatesWith(DUPLICATE_ITEM_1)
                    .prepare())
            .findAndFilterDuplicatesConfig(
                FilterDuplicatesConfig.<MultiKeyBean, ValidationFailure>toValidate()
                    .findAndFilterDuplicatesWith(
                        container ->
                            container.getId2() == null
                                ? null
                                : container.getId2().get18CharIdIfValid())
                    .andFailDuplicatesWith(DUPLICATE_ITEM_2)
                    .prepare())
            .prepare();
    final var results =
        List.ofAll(
            findAndFilterInvalids(
                validatables.toJavaList(),
                NOTHING_TO_VALIDATE,
                batchValidationConfig.findAndFilterDuplicatesConfigs));

    final var failedInvalids = results.take(2);
    assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);
    final var failedDuplicates1 = results.drop(2).take(2);
    assertThat(failedDuplicates1).allMatch(r -> r.getLeft() == DUPLICATE_ITEM_1);

    final var failedDuplicates2 = results.drop(4).take(1);
    assertThat(failedDuplicates2).allMatch(r -> r.getLeft() == DUPLICATE_ITEM_2);

    final var valids = results.drop(5);
    assertTrue(valids.forAll(Either::isRight));
    valids.forEachWithIndex(
        (r, i) -> assertEquals(String.valueOf(i + 1), r.get().getId1().toString()));
  }
  // end::batch-bean-multikey-demo[]

  @DisplayName("FailForDuplicates NOT configured. FAIL: NullValidatables, FILTER_ONLY: Duplicates")
  @Test
  void failNullValidatablesAndFilterDuplicates() {
    final List<Bean> nullValidatables = List.of(null, null);
    final var duplicateValidatables =
        List.of(
            new Bean(new ID("802xx000001ni4xAAA")),
            new Bean(new ID("802xx000001ni4x")),
            new Bean(new ID("802xx000001ni4x")));
    final var validatables =
        nullValidatables
            .appendAll(duplicateValidatables)
            .appendAll(
                List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesConfig(
                FilterDuplicatesConfig.<Bean, ValidationFailure>toValidate()
                    .findAndFilterDuplicatesWith(
                        container ->
                            container.getId() == null
                                ? null
                                : container.getId().get18CharIdIfValid())
                    .prepare())
            .prepare();
    final var results =
        List.ofAll(
            findAndFilterInvalids(
                validatables.toJavaList(),
                NOTHING_TO_VALIDATE,
                batchValidationConfig.findAndFilterDuplicatesConfigs));

    assertThat(results).hasSize(5);
    final var failedInvalids = results.take(2);
    assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);

    final var valids = results.drop(2);
    assertTrue(valids.forAll(Either::isRight));
    valids.forEachWithIndex(
        (r, i) -> assertEquals(String.valueOf(i + 1), r.get().getId().toString()));
  }

  @DisplayName(
      "FailForDuplicates NOT configured. FAIL: Null Validatables, FAIL: Null Keys, FILTER_ONLY: Duplicates")
  @Test
  void failNullValidatablesAndNullKeysAndFilterDuplicates() {
    final List<Bean> invalidValidatables = List.of(null, null);
    final var duplicateValidatables =
        List.of(
            new Bean(new ID("802xx000001ni4xAAA")),
            new Bean(new ID("802xx000001ni4x")),
            new Bean(new ID("802xx000001ni4x")));
    final var validatablesWithNullKeys = List.of(new Bean(null), new Bean(null));
    final var validatables =
        invalidValidatables
            .appendAll(duplicateValidatables)
            .appendAll(validatablesWithNullKeys)
            .appendAll(
                List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesConfig(
                FilterDuplicatesConfig.<Bean, ValidationFailure>toValidate()
                    .findAndFilterDuplicatesWith(
                        container ->
                            container.getId() == null
                                ? null
                                : container.getId().get18CharIdIfValid())
                    .andFailNullKeysWith(NULL_KEY)
                    .prepare())
            .prepare();
    final var results =
        List.ofAll(
            findAndFilterInvalids(
                validatables.toJavaList(),
                NOTHING_TO_VALIDATE,
                batchValidationConfig.findAndFilterDuplicatesConfigs));

    assertThat(results).hasSize(validatables.size() - duplicateValidatables.size());
    final var failedInvalids = results.take(2);
    assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);

    final var nullKeyInvalids = results.drop(2).take(2);
    assertThat(nullKeyInvalids).allMatch(r -> r.getLeft() == NULL_KEY);

    final var valids = results.drop(4);
    assertTrue(valids.forAll(Either::isRight));
    valids.forEachWithIndex(
        (r, i) -> assertEquals(String.valueOf(i + 1), r.get().getId().toString()));
  }

  @DisplayName(
      "FailForDuplicates, FailForNullKeys NOT configured. FAIL: Null Validatables, PASS: Null Keys, FILTER_ONLY: Duplicates")
  @Test
  void failInvalidatablesAndPassNullKeysAndFilterDuplicates() {
    final List<Bean> invalidValidatables = List.of(null, null);
    final var duplicateValidatables =
        List.of(
            new Bean(new ID("802xx000001ni4xAAA")),
            new Bean(new ID("802xx000001ni4x")),
            new Bean(new ID("802xx000001ni4x")));
    final var validatablesWithNullKeys = List.of(new Bean(null), new Bean(null));
    final var validatables =
        invalidValidatables
            .appendAll(duplicateValidatables)
            .appendAll(validatablesWithNullKeys)
            .appendAll(
                List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesConfig(
                FilterDuplicatesConfig.<Bean, ValidationFailure>toValidate()
                    .findAndFilterDuplicatesWith(
                        container ->
                            container.getId() == null
                                ? null
                                : container.getId().get18CharIdIfValid())
                    .prepare())
            .prepare();
    final var results =
        List.ofAll(
            findAndFilterInvalids(
                validatables.toJavaList(),
                NOTHING_TO_VALIDATE,
                batchValidationConfig.findAndFilterDuplicatesConfigs));

    assertThat(results).hasSize(validatables.size() - duplicateValidatables.size());
    final var failedInvalids = results.take(2);
    assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);

    final var nullKeyInvalids = results.drop(2).take(2);
    assertThat(nullKeyInvalids).allMatch(r -> r.get().equals(new Bean(null)));

    final var valids = results.drop(4);
    assertTrue(valids.forAll(Either::isRight));
    valids.forEachWithIndex(
        (r, i) -> assertEquals(String.valueOf(i + 1), r.get().getId().toString()));
  }

  @DisplayName("First Failure : Null validatable")
  @Test
  void filterInvalidatablesAndFailDuplicatesForAllOrNoneNullValidatables() {
    final List<Bean> invalidValidatables = List.of(null, null);
    final var duplicateValidatables =
        List.of(
            new Bean(new ID("802xx000001ni4xAAA")),
            new Bean(new ID("802xx000001ni4x")),
            new Bean(new ID("802xx000001ni4x")));
    final var validatables =
        invalidValidatables
            .appendAll(duplicateValidatables)
            .appendAll(
                List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesConfig(
                FilterDuplicatesConfig.<Bean, ValidationFailure>toValidate()
                    .findAndFilterDuplicatesWith(container -> container.getId().toString())
                    .andFailDuplicatesWith(DUPLICATE_ITEM)
                    .prepare())
            .prepare();
    final var result =
        findFirstInvalid(
            validatables.toJavaList(),
            NOTHING_TO_VALIDATE,
            batchValidationConfig.findAndFilterDuplicatesConfigs);
    assertThat(result).contains(NOTHING_TO_VALIDATE);
  }

  @Test
  void filterInvalidatablesAndDuplicatesForAllOrNone() {
    final var duplicateValidatables =
        List.of(new Bean(new ID("0")), new Bean(new ID("0")), new Bean(new ID("0")));
    final var validatables =
        duplicateValidatables.appendAll(
            List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesConfig(
                FilterDuplicatesConfig.<Bean, ValidationFailure>toValidate()
                    .findAndFilterDuplicatesWith(container -> container.getId().toString())
                    .prepare())
            .prepare();
    final var result =
        findFirstInvalid(
            validatables.toJavaList(),
            NOTHING_TO_VALIDATE,
            batchValidationConfig.findAndFilterDuplicatesConfigs);
    assertThat(result).isEmpty();
  }

  @Test
  void filterInvalidatablesAndDuplicatesAndFailNullKeysForAllOrNone() {
    final var duplicateValidatables =
        List.of(new Bean(new ID("0")), new Bean(new ID("0")), new Bean(new ID("0")));
    final var nullKeyValidatables = List.of(new Bean(null), new Bean(null));
    final var validatables =
        duplicateValidatables
            .appendAll(nullKeyValidatables)
            .appendAll(
                List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesConfig(
                FilterDuplicatesConfig.<Bean, ValidationFailure>toValidate()
                    .findAndFilterDuplicatesWith(
                        container ->
                            container.getId() == null ? null : container.getId().toString())
                    .andFailNullKeysWith(NULL_KEY)
                    .prepare())
            .prepare();
    final var result =
        findFirstInvalid(
            validatables.toJavaList(),
            NOTHING_TO_VALIDATE,
            batchValidationConfig.findAndFilterDuplicatesConfigs);
    assertThat(result).contains(NULL_KEY);
  }

  @Test
  void filterInvalidatablesAndDuplicatesForAllOrNoneDuplicate() {
    final var duplicateValidatables =
        List.of(new Bean(new ID("0")), new Bean(new ID("0")), new Bean(new ID("0")));
    final var validatables =
        duplicateValidatables.appendAll(
            List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesConfig(
                FilterDuplicatesConfig.<Bean, ValidationFailure>toValidate()
                    .findAndFilterDuplicatesWith(container -> container.getId().toString())
                    .andFailDuplicatesWith(DUPLICATE_ITEM)
                    .prepare())
            .prepare();
    final var result =
        findFirstInvalid(
            validatables.toJavaList(),
            NOTHING_TO_VALIDATE,
            batchValidationConfig.findAndFilterDuplicatesConfigs);
    assertThat(result).contains(DUPLICATE_ITEM);
  }

  @Test
  void filterInvalidatablesAndDuplicatesForAllOrNoneAllValid() {
    final var validatables =
        List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3")));
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .findAndFilterDuplicatesConfig(
                FilterDuplicatesConfig.<Bean, ValidationFailure>toValidate()
                    .findAndFilterDuplicatesWith(container -> container.getId().toString())
                    .andFailDuplicatesWith(DUPLICATE_ITEM)
                    .prepare())
            .prepare();
    final var result =
        findFirstInvalid(
            validatables.toJavaList(),
            NOTHING_TO_VALIDATE,
            batchValidationConfig.findAndFilterDuplicatesConfigs);
    assertThat(result).isEmpty();
  }

  @Value
  // tag::batch-bean[]
  private static class Bean {
    ID id;
  }
  // end::batch-bean[]

  @Value
  // tag::batch-bean-multikey[]
  private static class MultiKeyBean {
    ID id1;
    ID id2;
  }
  // end::batch-bean-multikey[]
}
