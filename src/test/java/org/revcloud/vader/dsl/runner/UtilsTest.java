package org.revcloud.vader.dsl.runner;

import com.force.swag.id.ID;
import consumer.bean.Container;
import consumer.failure.ValidationFailure;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static consumer.failure.ValidationFailure.DUPLICATE_ITEM;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {

    @Test
    void filterInvalidatablesAndFailDuplicates() {
        final List<Container> invalidValidatables = List.of(null, null);
        val duplicateValidatables = List.of(new Container(new ID("0")), new Container(new ID("0")), new Container(new ID("0")));
        val validatables = invalidValidatables.appendAll(duplicateValidatables).appendAll(List.of(
                new Container(new ID("1")), new Container(new ID("2")), new Container(new ID("3"))));

        val batchValidationConfig = BatchValidationConfig.<Container, ValidationFailure>toValidate()
                .findDuplicatesWith(container -> container.getSfId().toString())
                .andFailDuplicatesWith(DUPLICATE_ITEM).prepare();
        val results = Utils.filterInvalidatablesAndDuplicates(validatables, NOTHING_TO_VALIDATE, batchValidationConfig);
        
        val failedInvalids = results.take(2);
        Assertions.assertTrue(failedInvalids.forAll(Either::isLeft) &&
                failedInvalids.forAll(r -> r.getLeft() == NOTHING_TO_VALIDATE));
        val failedDuplicates = results.drop(2).take(3);
        Assertions.assertTrue(failedDuplicates.forAll(Either::isLeft) &&
                failedDuplicates.forAll(r -> r.getLeft() == DUPLICATE_ITEM));

        val valids = results.drop(5);
        Assertions.assertTrue(valids.forAll(Either::isRight));
        valids.forEachWithIndex((r, i) -> assertEquals(String.valueOf(i + 1), r.get().getSfId().toString()));
    }

    @Test
    void failInvalidatablesAndFilterDuplicates() {
        final List<Container> invalidValidatables = List.of(null, null);
        val duplicateValidatables = List.of(new Container(new ID("0")), new Container(new ID("0")), new Container(new ID("0")));
        val validatables = invalidValidatables.appendAll(duplicateValidatables).appendAll(List.of(
                new Container(new ID("1")), new Container(new ID("2")), new Container(new ID("3"))));

        val batchValidationConfig = BatchValidationConfig.<Container, ValidationFailure>toValidate()
                .findDuplicatesWith(container -> container.getSfId().toString()).prepare();
        val results = Utils.filterInvalidatablesAndDuplicates(validatables, NOTHING_TO_VALIDATE, batchValidationConfig);

        assertThat(results).hasSize(5);
        val failedInvalids = results.take(2);
        assertThat(failedInvalids).allMatch(r -> r.getLeft() == NOTHING_TO_VALIDATE);

        val valids = results.drop(2);
        Assertions.assertTrue(valids.forAll(Either::isRight));
        valids.forEachWithIndex((r, i) -> assertEquals(String.valueOf(i + 1), r.get().getSfId().toString()));
    }

    @Test
    void filterInvalidatablesAndFailDuplicatesForAllOrNoneInvalidValidatables() {
        final List<Container> invalidValidatables = List.of(null, null);
        val duplicateValidatables = List.of(new Container(new ID("0")), new Container(new ID("0")), new Container(new ID("0")));
        val validatables = invalidValidatables.appendAll(duplicateValidatables).appendAll(List.of(
                new Container(new ID("1")), new Container(new ID("2")), new Container(new ID("3"))));

        val batchValidationConfig = BatchValidationConfig.<Container, ValidationFailure>toValidate()
                .findDuplicatesWith(container -> container.getSfId().toString()).andFailDuplicatesWith(DUPLICATE_ITEM).prepare();
        val result = Utils.filterInvalidatablesAndDuplicatesForAllOrNone(validatables, NOTHING_TO_VALIDATE, batchValidationConfig);
        assertThat(result).contains(NOTHING_TO_VALIDATE);
    }

    @Test
    void filterInvalidatablesAndDuplicatesForAllOrNoneInvalidValidatables() {
        val duplicateValidatables = List.of(new Container(new ID("0")), new Container(new ID("0")), new Container(new ID("0")));
        val validatables = duplicateValidatables.appendAll(List.of(
                new Container(new ID("1")), new Container(new ID("2")), new Container(new ID("3"))));

        val batchValidationConfig = BatchValidationConfig.<Container, ValidationFailure>toValidate()
                .findDuplicatesWith(container -> container.getSfId().toString()).prepare();
        val result = Utils.filterInvalidatablesAndDuplicatesForAllOrNone(validatables, NOTHING_TO_VALIDATE, batchValidationConfig);
        assertThat(result).isEmpty();
    }


    @Test
    void filterInvalidatablesAndDuplicatesForAllOrNoneDuplicate() {
        val duplicateValidatables = List.of(new Container(new ID("0")), new Container(new ID("0")), new Container(new ID("0")));
        val validatables = duplicateValidatables.appendAll(List.of(
                new Container(new ID("1")), new Container(new ID("2")), new Container(new ID("3"))));

        val batchValidationConfig = BatchValidationConfig.<Container, ValidationFailure>toValidate()
                .findDuplicatesWith(container -> container.getSfId().toString()).andFailDuplicatesWith(DUPLICATE_ITEM).prepare();
        val result = Utils.filterInvalidatablesAndDuplicatesForAllOrNone(validatables, NOTHING_TO_VALIDATE, batchValidationConfig);
        assertThat(result).contains(DUPLICATE_ITEM);
    }

    @Test
    void filterInvalidatablesAndDuplicatesForAllOrNone() {
        val validatables = List.of(
                new Container(new ID("1")), new Container(new ID("2")), new Container(new ID("3")));

        val batchValidationConfig = BatchValidationConfig.<Container, ValidationFailure>toValidate()
                .findDuplicatesWith(container -> container.getSfId().toString()).andFailDuplicatesWith(DUPLICATE_ITEM).prepare();
        val result = Utils.filterInvalidatablesAndDuplicatesForAllOrNone(validatables, NOTHING_TO_VALIDATE, batchValidationConfig);
        assertThat(result).isEmpty();
    }
}
