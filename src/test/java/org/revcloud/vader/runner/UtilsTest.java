package org.revcloud.vader.runner;

import com.force.swag.id.ID;
import consumer.failure.ValidationFailure;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Test;

import static consumer.failure.ValidationFailure.DUPLICATE_ITEM;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static consumer.failure.ValidationFailure.NULL_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsTest {

    @Test
    void filterInvalidatablesAndFailDuplicates() {
        final List<Bean> invalidValidatables = List.of(null, null);
        val duplicateValidatables = List.of(new Bean(new ID("802xx000001ni4xAAA")), new Bean(new ID("802xx000001ni4x")), new Bean(new ID("802xx000001ni4x")));
        val validatables = invalidValidatables.appendAll(duplicateValidatables).appendAll(List.of(
                new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

        val batchValidationConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate()
                .findAndFilterDuplicatesWith(container -> container.getId().get18CharIdIfValid())
                .andFailDuplicatesWith(DUPLICATE_ITEM).prepare();
        val results = Utils.filterInvalidatablesAndDuplicates(validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);

        val failedInvalids = results.take(2);
        assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);
        val failedDuplicates = results.drop(2).take(3);
        assertThat(failedDuplicates).allMatch(r -> r.getLeft() == DUPLICATE_ITEM);

        val valids = results.drop(5);
        assertTrue(valids.forAll(Either::isRight));
        valids.forEachWithIndex((r, i) -> assertEquals(String.valueOf(i + 1), r.get().getId().toString()));
    }

    @Test
    void failInvalidatablesAndFilterDuplicates() {
        final List<Bean> invalidValidatables = List.of(null, null);
        val duplicateValidatables = List.of(new Bean(new ID("802xx000001ni4xAAA")), new Bean(new ID("802xx000001ni4x")), new Bean(new ID("802xx000001ni4x")));
        val validatables = invalidValidatables.appendAll(duplicateValidatables).appendAll(List.of(
                new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

        val batchValidationConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate()
                .findAndFilterDuplicatesWith(container -> container.getId().get18CharIdIfValid()).prepare();
        val results = Utils.filterInvalidatablesAndDuplicates(validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);

        assertThat(results).hasSize(5);
        val failedInvalids = results.take(2);
        assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);

        val valids = results.drop(2);
        assertTrue(valids.forAll(Either::isRight));
        valids.forEachWithIndex((r, i) -> assertEquals(String.valueOf(i + 1), r.get().getId().toString()));
    }

    @Test
    void failInvalidatablesAndNullKeysAndFilterDuplicates() {
        final List<Bean> invalidValidatables = List.of(null, null);
        val duplicateValidatables = List.of(new Bean(new ID("802xx000001ni4xAAA")), new Bean(new ID("802xx000001ni4x")), new Bean(new ID("802xx000001ni4x")));
        val validatablesWithNullKeys = List.of(new Bean(null), new Bean(null));
        val validatables = invalidValidatables.appendAll(duplicateValidatables).appendAll(validatablesWithNullKeys)
                .appendAll(List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

        val batchValidationConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate()
                .findAndFilterDuplicatesWith(container -> container.getId() == null ? null : container.getId().get18CharIdIfValid())
                .andFailNullKeysWith(NULL_KEY)
                .prepare();
        val results = Utils.filterInvalidatablesAndDuplicates(validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);

        assertThat(results).hasSize(validatables.size() - duplicateValidatables.size());
        val failedInvalids = results.take(2);
        assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);

        val nullKeyInvalids = results.drop(2).take(2);
        assertThat(nullKeyInvalids).allMatch(r -> r.getLeft() == NULL_KEY);

        val valids = results.drop(4);
        assertTrue(valids.forAll(Either::isRight));
        valids.forEachWithIndex((r, i) -> assertEquals(String.valueOf(i + 1), r.get().getId().toString()));
    }

    @Test
    void failInvalidatablesAndPassNullKeysAndFilterDuplicates() {
        final List<Bean> invalidValidatables = List.of(null, null);
        val duplicateValidatables = List.of(new Bean(new ID("802xx000001ni4xAAA")), new Bean(new ID("802xx000001ni4x")), new Bean(new ID("802xx000001ni4x")));
        val validatablesWithNullKeys = List.of(new Bean(null), new Bean(null));
        val validatables = invalidValidatables.appendAll(duplicateValidatables).appendAll(validatablesWithNullKeys)
                .appendAll(List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

        val batchValidationConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate()
                .findAndFilterDuplicatesWith(container -> container.getId() == null ? null : container.getId().get18CharIdIfValid())
                .prepare();
        val results = Utils.filterInvalidatablesAndDuplicates(validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);

        assertThat(results).hasSize(validatables.size() - duplicateValidatables.size());
        val failedInvalids = results.take(2);
        assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);

        val nullKeyInvalids = results.drop(2).take(2);
        assertThat(nullKeyInvalids).allMatch(r -> r.get().equals(new Bean(null)));

        val valids = results.drop(4);
        assertTrue(valids.forAll(Either::isRight));
        valids.forEachWithIndex((r, i) -> assertEquals(String.valueOf(i + 1), r.get().getId().toString()));
    }

    @Test
    void filterInvalidatablesAndFailDuplicatesForAllOrNoneInvalidValidatables() {
        final List<Bean> invalidValidatables = List.of(null, null);
        val duplicateValidatables = List.of(new Bean(new ID("802xx000001ni4xAAA")), new Bean(new ID("802xx000001ni4x")), new Bean(new ID("802xx000001ni4x")));
        val validatables = invalidValidatables.appendAll(duplicateValidatables).appendAll(List.of(
                new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

        val batchValidationConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate()
                .findAndFilterDuplicatesWith(container -> container.getId().toString()).andFailDuplicatesWith(DUPLICATE_ITEM).prepare();
        val result = Utils.filterInvalidatablesAndDuplicatesForAllOrNone(validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);
        assertThat(result).contains(NOTHING_TO_VALIDATE);
    }

    @Test
    void filterInvalidatablesAndDuplicatesForAllOrNone() {
        val duplicateValidatables = List.of(new Bean(new ID("0")), new Bean(new ID("0")), new Bean(new ID("0")));
        val validatables = duplicateValidatables.appendAll(List.of(
                new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));
        val batchValidationConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate()
                .findAndFilterDuplicatesWith(container -> container.getId().toString()).prepare();
        val result = Utils.filterInvalidatablesAndDuplicatesForAllOrNone(validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);
        assertThat(result).isEmpty();
    }

    @Test
    void filterInvalidatablesAndDuplicatesAndFailNullKeysForAllOrNone() {
        val duplicateValidatables = List.of(new Bean(new ID("0")), new Bean(new ID("0")), new Bean(new ID("0")));
        val nullKeyValidatables = List.of(new Bean(null), new Bean(null));
        val validatables = duplicateValidatables.appendAll(nullKeyValidatables).appendAll(List.of(
                new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));
        val batchValidationConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate()
                .findAndFilterDuplicatesWith(container -> container.getId() == null ? null : container.getId().toString())
                .andFailNullKeysWith(NULL_KEY)
                .prepare();
        val result = Utils.filterInvalidatablesAndDuplicatesForAllOrNone(validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);
        assertThat(result).contains(NULL_KEY);
    }

    @Test
    void filterInvalidatablesAndDuplicatesForAllOrNoneDuplicate() {
        val duplicateValidatables = List.of(new Bean(new ID("0")), new Bean(new ID("0")), new Bean(new ID("0")));
        val validatables = duplicateValidatables.appendAll(List.of(
                new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3"))));

        val batchValidationConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate()
                .findAndFilterDuplicatesWith(container -> container.getId().toString()).andFailDuplicatesWith(DUPLICATE_ITEM).prepare();
        val result = Utils.filterInvalidatablesAndDuplicatesForAllOrNone(validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);
        assertThat(result).contains(DUPLICATE_ITEM);
    }

    @Test
    void filterInvalidatablesAndDuplicatesForAllOrNoneAllValid() {
        val validatables = List.of(new Bean(new ID("1")), new Bean(new ID("2")), new Bean(new ID("3")));
        val batchValidationConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate()
                .findAndFilterDuplicatesWith(container -> container.getId().toString()).andFailDuplicatesWith(DUPLICATE_ITEM).prepare();
        val result = Utils.filterInvalidatablesAndDuplicatesForAllOrNone(validatables.toJavaList(), NOTHING_TO_VALIDATE, batchValidationConfig);
        assertThat(result).isEmpty();
    }

    @Value
    private static class Bean {
        ID id;
    }
}
