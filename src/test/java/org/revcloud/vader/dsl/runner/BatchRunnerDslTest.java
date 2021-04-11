package org.revcloud.vader.dsl.runner;

import consumer.bean.Parent;
import consumer.failure.ValidationFailure;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.Validator;
import org.revcloud.vader.types.validators.SimpleValidator;

import java.util.function.Predicate;

import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.VALIDATION_FAILURE_1;
import static consumer.failure.ValidationFailure.VALIDATION_FAILURE_2;
import static consumer.failure.ValidationFailure.VALIDATION_FAILURE_3;

/**
 * gakshintala created on 7/22/20.
 */
@Slf4j
class BatchRunnerDslTest {

    @Test
    void failFastPartialFailures() {
        val validatables = List.of(new Parent(0, null, null), new Parent(1, null, null), new Parent(2, null, null),
                new Parent(3, null, null), new Parent(4, null, null));
        Predicate<Integer> predicateForValidId1 = id -> id >= 2;
        Predicate<Integer> predicateForValidId2 = id -> id <= 2;
        List<Validator<Parent, ValidationFailure>> validators = List.of(
                parent -> Either.right(true),
                parent -> parent.map(Parent::getId).filterOrElse(predicateForValidId1, ignore -> VALIDATION_FAILURE_1),
                parent -> parent.map(Parent::getId).filterOrElse(predicateForValidId2, ignore -> VALIDATION_FAILURE_2)
        );
        val results = BatchRunnerDsl.validateAndFailFast(validatables, validators, null, throwable -> null);
        results.forEach(result -> log.info(result.toString()));
        Assertions.assertEquals(results.size(), validatables.size());
        Assertions.assertTrue(results.get(2).isRight());
        Assertions.assertEquals(results.get(2), Either.right(new Parent(2, null, null)));
        Assertions.assertTrue(results.take(2).forAll(vf -> vf.isLeft() && vf.getLeft() == VALIDATION_FAILURE_1));
        Assertions.assertTrue(results.takeRight(2).forAll(vf -> vf.isLeft() && vf.getLeft() == VALIDATION_FAILURE_2));
    }

    @Test
    void failFastPartialFailuresForSimpleValidators() {
        val validatables = List.of(new Parent(0, null, null), new Parent(1, null, null), new Parent(2, null, null),
                new Parent(3, null, null), new Parent(4, null, null));
        Predicate<Integer> predicateForValidId1 = id -> id >= 2;
        Predicate<Integer> predicateForValidId2 = id -> id <= 2;
        List<SimpleValidator<Parent, ValidationFailure>> simpleValidators = List.of(
                parent -> NONE,
                parent -> predicateForValidId1.test(parent.getId()) ? NONE : VALIDATION_FAILURE_1,
                parent -> predicateForValidId2.test(parent.getId()) ? NONE : VALIDATION_FAILURE_2
        );
        val result = BatchRunnerDsl.validateAndFailFast(validatables, simpleValidators, null, NONE, throwable -> null);
        Assertions.assertEquals(result.size(), validatables.size());
        Assertions.assertTrue(result.get(2).isRight());
        Assertions.assertEquals(result.get(2), Either.right(new Parent(2, null, null)));
        Assertions.assertTrue(result.take(2).forAll(vf -> vf.isLeft() && vf.getLeft() == VALIDATION_FAILURE_1));
        Assertions.assertTrue(result.takeRight(2).forAll(vf -> vf.isLeft() && vf.getLeft() == VALIDATION_FAILURE_2));
    }

    @Test
    void errorAccumulateForSimpleValidators() {
        val validatables = List.of(new Parent(0, null, null), new Parent(1, null, null), new Parent(2, null, null),
                new Parent(3, null, null), new Parent(4, null, null));
        Predicate<Integer> predicateForValidId1 = id -> id >= 2;
        Predicate<Integer> predicateForValidId2 = id -> id <= 2;
        Predicate<Integer> predicateForValidId3 = id -> id >= 1;
        List<SimpleValidator<Parent, ValidationFailure>> simpleValidators = List.of(
                parent -> NONE,
                parent -> predicateForValidId1.test(parent.getId()) ? NONE : VALIDATION_FAILURE_1,
                parent -> predicateForValidId2.test(parent.getId()) ? NONE : VALIDATION_FAILURE_2,
                parent -> predicateForValidId3.test(parent.getId()) ? NONE : VALIDATION_FAILURE_3
        );
        val result = BatchRunnerDsl.validateAndAccumulateErrors(validatables, simpleValidators, null, NONE, throwable -> null);

        Assertions.assertEquals(result.size(), validatables.size());
        Assertions.assertTrue(result.forAll(r -> r.size() == simpleValidators.size()));

        Assertions.assertTrue(result.get(2).forAll(Either::isRight));
        Assertions.assertTrue(result.get(2).forAll(resultPerValidation -> Either.right(new Parent(2, null, null)).equals(resultPerValidation)));

        Assertions.assertTrue(result.take(2).forAll(vf -> vf.get(1).isLeft() && vf.get(1).getLeft() == VALIDATION_FAILURE_1));
        result.take(2).forEachWithIndex((vf, index) -> 
                Assertions.assertTrue(vf.get(2).isRight() && Either.right(new Parent(index, null, null)).equals(vf.get(2))));

        Assertions.assertTrue(result.takeRight(2).forAll(vf -> vf.get(2).isLeft() && vf.get(2).getLeft() == VALIDATION_FAILURE_2));
        result.takeRight(2).forEachWithIndex((vf, index) ->
                Assertions.assertTrue(vf.get(1).isRight() && Either.right(new Parent(validatables.size() - 2 + index, null, null)).equals(vf.get(1))));
        
        Assertions.assertEquals(result.get(0).get(3), Either.left(VALIDATION_FAILURE_3));
    }
}
