package org.revcloud.vader.dsl.runner;

import com.force.swag.id.ID;
import consumer.bean.Container;
import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static consumer.failure.ValidationFailure.DUPLICATE_ITEM;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StrategiesTest {

    @Test
    void filterInvalidatablesAndDuplicates() {
        final List<Container> invalidValidatables = List.of(null, null);
        final var duplicateValidatables = List.of(new Container(new ID("0")), new Container(new ID("0")), new Container(new ID("0")));
        val validatables = invalidValidatables.appendAll(duplicateValidatables).appendAll(List.of(
                new Container(new ID("1")), new Container(new ID("2")), new Container(new ID("3"))
        ));
        
        val batchValidationConfig = BatchValidationConfig.<Container, ValidationFailure>toValidate()
                .shouldFilterDuplicates(Tuple.of(DUPLICATE_ITEM, container -> container.getSfId().toString())).prepare();
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
}
