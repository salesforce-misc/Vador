package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.VALIDATION_FAILURE_1;
import static consumer.failure.ValidationFailure.VALIDATION_FAILURE_2;
import static consumer.failure.ValidationFailure.VALIDATION_FAILURE_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.revcloud.vader.runner.BatchRunner.validateAndFailFastForEach;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.Validator;
import org.revcloud.vader.types.validators.ValidatorEtr;

/** gakshintala created on 7/22/20. */
class BatchRunnerTest {

  @Test
  void failFastPartialFailures() {
    final var validatables =
        List.of(new Bean(0), new Bean(1), new Bean(2), new Bean(3), new Bean(4));
    List<ValidatorEtr<Bean, ValidationFailure>> validators =
        List.of(
            bean -> Either.right(true),
            bean ->
                bean.map(Bean::getId).filterOrElse(id -> id >= 2, ignore -> VALIDATION_FAILURE_1),
            bean ->
                bean.map(Bean::getId).filterOrElse(id -> id <= 2, ignore -> VALIDATION_FAILURE_2));
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .withValidatorEtrs(validators)
            .prepare();
    final var resultsWithIds =
        validateAndFailFastForEach(
            validatables,
            Bean::getId,
            batchValidationConfig);

    final var ids =
        resultsWithIds.stream()
            .map(etr -> etr.fold(Tuple2::_1, Bean::getId))
            .collect(Collectors.toList());
    assertThat(ids).containsAll(IntStream.rangeClosed(0, 4).boxed().collect(Collectors.toList()));

    final var results =
        resultsWithIds.stream().map(etr -> etr.mapLeft(Tuple2::_2)).collect(Collectors.toList());
    assertEquals(validatables.size(), results.size());
    assertThat(results.get(2)).containsOnRight(new Bean(2));
    assertThat(results.stream().limit(2)).containsOnly(Either.left(VALIDATION_FAILURE_1));
    assertThat(results.stream().skip(results.size() - 2))
        .containsOnly(Either.left(VALIDATION_FAILURE_2));
  }

  @Test
  void failFastForAny() {
    final var validatables =
        List.of(new Bean(0), new Bean(1), new Bean(2), new Bean(3), new Bean(4));
    List<ValidatorEtr<Bean, ValidationFailure>> validators =
        List.of(
            bean -> Either.right(true),
            bean -> // Fail if id < 2
            bean.map(Bean::getId).filterOrElse(id -> id >= 2, ignore -> VALIDATION_FAILURE_1),
            bean ->
                bean.map(Bean::getId).filterOrElse(id -> id <= 2, ignore -> VALIDATION_FAILURE_2));
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .withValidatorEtrs(validators)
            .prepare();
    final var result =
        BatchRunner.validateAndFailFastForAny(
            validatables,
            batchValidationConfig);
    assertThat(result).contains(VALIDATION_FAILURE_1);
  }

  @Test
  void failFastForAnyWithIdMapper() {
    final var validatables =
        List.of(new Bean(0), new Bean(1), new Bean(2), new Bean(3), new Bean(4));
    List<ValidatorEtr<Bean, ValidationFailure>> validators =
        List.of(
            bean -> Either.right(true),
            bean ->
                bean.map(Bean::getId).filterOrElse(id -> id >= 0, ignore -> VALIDATION_FAILURE_1),
            bean ->
                bean.map(Bean::getId).filterOrElse(id -> id != 2, ignore -> VALIDATION_FAILURE_2));
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .withValidatorEtrs(validators)
            .prepare();
    final var result =
        BatchRunner.validateAndFailFastForAny(
            validatables,
            Bean::getId, 
            batchValidationConfig);
    assertThat(result).contains(Tuple.of(2, VALIDATION_FAILURE_2));
  }

  @Test
  void failFastPartialFailuresForValidators() {
    final var validatables =
        List.of(new Bean(0), new Bean(1), new Bean(2), new Bean(3), new Bean(4));
    var predicateForValidId1 = (Predicate<Integer>) id -> id >= 2;
    var predicateForValidId2 = (Predicate<Integer>) id -> id <= 2;
    List<Validator<Bean, ValidationFailure>> validators =
        List.of(
            bean -> NONE,
            bean -> predicateForValidId1.test(bean.getId()) ? NONE : VALIDATION_FAILURE_1,
            bean -> predicateForValidId2.test(bean.getId()) ? NONE : VALIDATION_FAILURE_2);
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .withValidators(Tuple.of(validators, NONE))
            .prepare();
    final var results =
        BatchRunner.validateAndFailFastForEach(
            validatables,
            batchValidationConfig);
    assertEquals(results.size(), validatables.size());
    assertTrue(results.get(2).isRight());
    assertEquals(results.get(2), Either.right(new Bean(2)));
    assertTrue(
        results.stream()
            .limit(2)
            .allMatch(vf -> vf.isLeft() && vf.getLeft() == VALIDATION_FAILURE_1));
    assertTrue(
        results.stream()
            .skip(results.size() - 2)
            .allMatch(vf -> vf.isLeft() && vf.getLeft() == VALIDATION_FAILURE_2));
  }

  @Test
  void errorAccumulateForValidators() {
    final var validatables =
        List.of(new Bean(0), new Bean(1), new Bean(2), new Bean(3), new Bean(4));
    var predicateForValidId1 = (Predicate<Integer>) id -> id >= 2;
    var predicateForValidId2 = (Predicate<Integer>) id -> id <= 2;
    var predicateForValidId3 = (Predicate<Integer>) id -> id >= 1;
    List<Validator<Bean, ValidationFailure>> validators =
        List.of(
            bean -> NONE,
            bean -> predicateForValidId1.test(bean.getId()) ? NONE : VALIDATION_FAILURE_1,
            bean -> predicateForValidId2.test(bean.getId()) ? NONE : VALIDATION_FAILURE_2,
            bean -> predicateForValidId3.test(bean.getId()) ? NONE : VALIDATION_FAILURE_3);
    final var result =
        BatchRunner.validateAndAccumulateErrors(validatables, validators, NONE, throwable -> null);

    assertEquals(result.size(), validatables.size());
    assertTrue(result.stream().allMatch(r -> r.size() == validators.size()));

    assertTrue(result.get(2).stream().allMatch(Either::isRight));
    assertTrue(
        result.get(2).stream()
            .allMatch(
                resultPerValidation -> Either.right(new Bean(2)).equals(resultPerValidation)));

    assertTrue(
        result.stream()
            .limit(2)
            .allMatch(vf -> vf.get(1).isLeft() && vf.get(1).getLeft() == VALIDATION_FAILURE_1));

    io.vavr.collection.List.ofAll(result)
        .take(2)
        .forEachWithIndex(
            (vf, index) ->
                assertTrue(vf.get(2).isRight() && Either.right(new Bean(index)).equals(vf.get(2))));

    assertTrue(
        result.stream()
            .skip(result.size() - 2)
            .allMatch(vf -> vf.get(2).isLeft() && vf.get(2).getLeft() == VALIDATION_FAILURE_2));
    io.vavr.collection.List.ofAll(result)
        .takeRight(2)
        .forEachWithIndex(
            (vf, index) ->
                assertTrue(
                    vf.get(1).isRight()
                        && Either.right(new Bean(validatables.size() - 2 + index))
                            .equals(vf.get(1))));

    assertEquals(result.get(0).get(3), Either.left(VALIDATION_FAILURE_3));
  }

  @Value
  private static class Bean {
    int id;
  }
}
