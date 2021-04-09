package org.revcloud.vader.dsl.runner;

import com.force.swag.id.ID;
import consumer.bean.Parent;
import consumer.failure.ValidationFailure;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.config.BatchValidationConfig;

import static consumer.failure.ValidationFailure.DUPLICATE_ITEM;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static org.junit.jupiter.api.Assertions.*;

class StrategiesTest {

    @Test
    void filterInvalidatablesAndDuplicates() {
        final List<Parent> invalidValidatables = List.of(null, null);
        final var duplicateValidatables = List.of(new Parent(new ID("0")), new Parent(new ID("0")), new Parent(new ID("0")));
        val validatables = invalidValidatables.appendAll(duplicateValidatables).appendAll(List.of(
                new Parent(new ID("1")), new Parent(new ID("2")), new Parent(new ID("3"))
        ));
        
        val batchValidationConfig = BatchValidationConfig.toValidate(Parent.class, ValidationFailure.class)
                .failDuplicatesWith(DUPLICATE_ITEM, parent -> parent.getSfId().toString());
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
