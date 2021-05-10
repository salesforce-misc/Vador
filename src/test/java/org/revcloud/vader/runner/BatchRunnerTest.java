package org.revcloud.vader.runner;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import io.vavr.control.Either;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import java.util.List;
import java.util.function.Predicate;

import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.VALIDATION_FAILURE_1;
import static consumer.failure.ValidationFailure.VALIDATION_FAILURE_2;
import static consumer.failure.ValidationFailure.VALIDATION_FAILURE_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * gakshintala created on 7/22/20.
 */
class BatchRunnerTest {

    @Test
    void failFastPartialFailures() {
        final var validatables = List.of(new Bean(0), new Bean(1), new Bean(2), new Bean(3), new Bean(4));
        List<Validator<Bean, ValidationFailure>> validators = List.of(
                bean -> Either.right(true),
                bean -> bean.map(Bean::getId).filterOrElse(id -> id >= 2, ignore -> VALIDATION_FAILURE_1),
                bean -> bean.map(Bean::getId).filterOrElse(id -> id <= 2, ignore -> VALIDATION_FAILURE_2)
        );
        final var batchValidationConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate().withValidators(validators).prepare();
        final var results = BatchRunner.validateAndFailFast(validatables, NONE, ValidationFailure::getValidationFailureForException, batchValidationConfig);
        assertEquals(validatables.size(), results.size());
        assertTrue(results.get(2).isRight());
        assertEquals(results.get(2), Either.right(new Bean(2)));
        assertTrue(results.stream().limit(2).allMatch(vf -> vf.isLeft() && vf.getLeft() == VALIDATION_FAILURE_1));
        assertTrue(results.stream().skip(results.size() - 2).allMatch(vf -> vf.isLeft() && vf.getLeft() == VALIDATION_FAILURE_2));
    }

    @Test
    void failFastAllOrNone() {
        final var validatables = List.of(new Bean(0), new Bean(1), new Bean(2), new Bean(3), new Bean(4));
        List<Validator<Bean, ValidationFailure>> validators = List.of(
                bean -> Either.right(true),
                bean -> bean.map(Bean::getId).filterOrElse(id -> id >= 2, ignore -> VALIDATION_FAILURE_1),
                bean -> bean.map(Bean::getId).filterOrElse(id -> id <= 2, ignore -> VALIDATION_FAILURE_2)
        );
        final var batchValidationConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate().withValidators(validators).prepare();
        final var result = BatchRunner.validateAndFailFastAllOrNone(validatables, NONE, ValidationFailure::getValidationFailureForException, batchValidationConfig);
        assertThat(result).contains(VALIDATION_FAILURE_1);
    }

    @Test
    void failFastPartialFailuresForSimpleValidators() {
        final var validatables = List.of(new Bean(0), new Bean(1), new Bean(2),
                new Bean(3), new Bean(4));
        Predicate<Integer> predicateForValidId1 = id -> id >= 2;
        Predicate<Integer> predicateForValidId2 = id -> id <= 2;
        List<SimpleValidator<Bean, ValidationFailure>> simpleValidators = List.of(
                bean -> NONE,
                bean -> predicateForValidId1.test(bean.getId()) ? NONE : VALIDATION_FAILURE_1,
                bean -> predicateForValidId2.test(bean.getId()) ? NONE : VALIDATION_FAILURE_2
        );
        final var batchValidationConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate()
                .withSimpleValidatorsOrFailWith(Tuple.of(simpleValidators, NONE)).prepare();
        final var results = BatchRunner.validateAndFailFast(validatables, NONE, ValidationFailure::getValidationFailureForException, batchValidationConfig);
        assertEquals(results.size(), validatables.size());
        assertTrue(results.get(2).isRight());
        assertEquals(results.get(2), Either.right(new Bean(2)));
        assertTrue(results.stream().limit(2).allMatch(vf -> vf.isLeft() && vf.getLeft() == VALIDATION_FAILURE_1));
        assertTrue(results.stream().skip(results.size() - 2).allMatch(vf -> vf.isLeft() && vf.getLeft() == VALIDATION_FAILURE_2));
    }

    @Test
    void errorAccumulateForSimpleValidators() {
        final var validatables = List.of(new Bean(0), new Bean(1), new Bean(2),
                new Bean(3), new Bean(4));
        Predicate<Integer> predicateForValidId1 = id -> id >= 2;
        Predicate<Integer> predicateForValidId2 = id -> id <= 2;
        Predicate<Integer> predicateForValidId3 = id -> id >= 1;
        List<SimpleValidator<Bean, ValidationFailure>> simpleValidators = List.of(
                bean -> NONE,
                bean -> predicateForValidId1.test(bean.getId()) ? NONE : VALIDATION_FAILURE_1,
                bean -> predicateForValidId2.test(bean.getId()) ? NONE : VALIDATION_FAILURE_2,
                bean -> predicateForValidId3.test(bean.getId()) ? NONE : VALIDATION_FAILURE_3
        );
        final var result = BatchRunner.validateAndAccumulateErrors(validatables, simpleValidators, null, NONE, throwable -> null);

        assertEquals(result.size(), validatables.size());
        assertTrue(result.stream().allMatch(r -> r.size() == simpleValidators.size()));

        assertTrue(result.get(2).stream().allMatch(Either::isRight));
        assertTrue(result.get(2).stream().allMatch(resultPerValidation -> Either.right(new Bean(2)).equals(resultPerValidation)));

        assertTrue(result.stream().limit(2).allMatch(vf -> vf.get(1).isLeft() && vf.get(1).getLeft() == VALIDATION_FAILURE_1));

        io.vavr.collection.List.ofAll(result).take(2).forEachWithIndex((vf, index) ->
                assertTrue(vf.get(2).isRight() && Either.right(new Bean(index)).equals(vf.get(2))));

        assertTrue(result.stream().skip(result.size() - 2).allMatch(vf -> vf.get(2).isLeft() && vf.get(2).getLeft() == VALIDATION_FAILURE_2));
        io.vavr.collection.List.ofAll(result).takeRight(2).forEachWithIndex((vf, index) ->
                assertTrue(vf.get(1).isRight() && Either.right(new Bean(validatables.size() - 2 + index)).equals(vf.get(1))));

        assertEquals(result.get(0).get(3), Either.left(VALIDATION_FAILURE_3));
    }

    @Value
    private static class Bean {
        int id;
    }
}
