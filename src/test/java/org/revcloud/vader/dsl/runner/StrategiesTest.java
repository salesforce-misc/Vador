package org.revcloud.vader.dsl.runner;

import consumer.bean.Parent;
import consumer.failure.ValidationFailure;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.dsl.runner.config.BatchValidationConfig;

import static consumer.failure.ValidationFailure.DUPLICATE_ITEM;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static org.junit.jupiter.api.Assertions.*;

class StrategiesTest {

    @Test
    void filterInvalidatablesAndDuplicates() {
        final var duplicateValidatables = List.fill(3, new Parent(0));
        final List<Parent> invalidValidatables = List.of(null, null);
        val validatables = duplicateValidatables.appendAll(invalidValidatables).appendAll(List.of(
                new Parent(1), new Parent(2), new Parent(3)
        ));
        
        val batchValidationConfig = BatchValidationConfig.toValidate(Parent.class, ValidationFailure.class)
                .failDuplicatesWith(DUPLICATE_ITEM, Parent::getId);

        val results = Strategies.filterInvalidatablesAndDuplicates(validatables, NOTHING_TO_VALIDATE, batchValidationConfig);
        
        val failedForDuplicates = results.take(3);
        Assertions.assertTrue(failedForDuplicates.forAll(Either::isLeft) && 
                failedForDuplicates.forAll(r -> r.getLeft() == DUPLICATE_ITEM));
        val failedForInvalids = results.drop(3).take(2);
        Assertions.assertTrue(failedForInvalids.forAll(Either::isLeft) &&
                failedForInvalids.forAll(r -> r.getLeft() == NOTHING_TO_VALIDATE));
        
        val valids = results.drop(5);
        Assertions.assertTrue(valids.forAll(Either::isRight));
        valids.forEachWithIndex((r, i) -> assertEquals(i + 1, r.get().getId()));
    }
}
