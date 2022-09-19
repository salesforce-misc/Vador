/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vador.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sample.consumer.failure.ValidationFailure.NONE;
import static sample.consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static sample.consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;
import static sample.consumer.failure.ValidationFailure.VALIDATION_FAILURE_1;
import static sample.consumer.failure.ValidationFailure.VALIDATION_FAILURE_2;
import static sample.consumer.failure.ValidationFailure.VALIDATION_FAILURE_3;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Value;
import org.assertj.vavr.api.VavrAssertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vador.config.BatchValidationConfig;
import org.revcloud.vador.config.ValidationConfig;
import org.revcloud.vador.types.Validator;
import org.revcloud.vador.types.ValidatorEtr;
import sample.consumer.failure.ValidationFailure;

/** gakshintala created on 7/22/20. */
class VadorBatchTest {

  private static final List<Bean> VALIDATABLE_BATCH =
      List.of(new Bean(0), new Bean(1), new Bean(2), new Bean(3), new Bean(4));

  public static final Validator<Bean, ValidationFailure> validator1 = bean -> NONE;
  public static final Validator<Bean, ValidationFailure> validator2 =
      bean -> bean.getId() >= 2 ? NONE : VALIDATION_FAILURE_1;
  public static final Validator<Bean, ValidationFailure> validator3 =
      bean -> bean.getId() <= 2 ? NONE : VALIDATION_FAILURE_2;
  private static final List<Validator<Bean, ValidationFailure>> VALIDATORS =
      List.of(validator1, validator2, validator3);

  public static final ValidatorEtr<Bean, ValidationFailure> validatorEtr1 =
      bean -> Either.right(true);
  public static final ValidatorEtr<Bean, ValidationFailure> validatorEtr2 =
      bean -> bean.map(Bean::getId).filterOrElse(id -> id >= 2, ignore -> VALIDATION_FAILURE_1);
  public static final ValidatorEtr<Bean, ValidationFailure> validatorEtr3 =
      bean -> bean.map(Bean::getId).filterOrElse(id -> id <= 2, ignore -> VALIDATION_FAILURE_2);
  private static final List<ValidatorEtr<Bean, ValidationFailure>> VALIDATOR_ETRS =
      List.of(validatorEtr1, validatorEtr2, validatorEtr3);

  @Test
  void failFastPartialFailuresForValidators() {
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .withValidators(Tuple.of(VALIDATORS, NONE))
            .prepare();
    final var results =
        VadorBatch.validateAndFailFastForEach(VALIDATABLE_BATCH, batchValidationConfig);
    assertEquals(results.size(), VALIDATABLE_BATCH.size());
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
  void failFastPartialFailures() {
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .withValidatorEtrs(VALIDATOR_ETRS)
            .prepare();
    final var results =
        VadorBatch.validateAndFailFastForEach(VALIDATABLE_BATCH, batchValidationConfig);

    assertEquals(VALIDATABLE_BATCH.size(), results.size());
    assertThat(results.get(2)).containsOnRight(new Bean(2));
    assertThat(results.stream().limit(2)).containsOnly(Either.left(VALIDATION_FAILURE_1));
    assertThat(results.stream().skip(results.size() - 2))
        .containsOnly(Either.left(VALIDATION_FAILURE_2));
  }

  @Test
  void failFastPartialFailuresWithPair() {
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .withValidatorEtrs(VALIDATOR_ETRS)
            .prepare();
    final var resultsWithIds =
        VadorBatch.validateAndFailFastForEach(
            VALIDATABLE_BATCH, Bean::getId, batchValidationConfig);

    final var ids =
        resultsWithIds.stream()
            .map(etr -> etr.fold(Tuple2::_1, Bean::getId))
            .collect(Collectors.toList());
    assertThat(ids).containsAll(IntStream.rangeClosed(0, 4).boxed().collect(Collectors.toList()));
  }

  @Test
  void failFastForAny() {
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .withValidatorEtrs(VALIDATOR_ETRS)
            .prepare();
    final var result =
        VadorBatch.validateAndFailFastForAny(VALIDATABLE_BATCH, batchValidationConfig);
    assertThat(result).contains(VALIDATION_FAILURE_1);
  }

  @Test
  void failFastForAnyRecursively() {
    final Validator<RecursiveBean, ValidationFailure> validator =
        recursiveBean -> recursiveBean.id == -1 ? UNKNOWN_EXCEPTION : NONE;
    final var recursiveBeans = List.of(
        new RecursiveBean(
            1,
            List.of(
                new RecursiveBean(11, Collections.emptyList()),
                new RecursiveBean(12, Collections.emptyList()),
                new RecursiveBean(13, Collections.emptyList()))),
        new RecursiveBean(
            1,
            List.of(
                new RecursiveBean(11, Collections.emptyList()),
                new RecursiveBean(-1, Collections.emptyList()),
                new RecursiveBean(13, Collections.emptyList()))),
        new RecursiveBean(
            1,
            List.of(
                new RecursiveBean(11, Collections.emptyList()),
                new RecursiveBean(12, Collections.emptyList()),
                new RecursiveBean(13, Collections.emptyList()))));
    final var validationConfig =
        BatchValidationConfig.<RecursiveBean, ValidationFailure>toValidate()
            .withValidator(validator, NONE)
            .withRecursiveMapper(RecursiveBean::getRecursiveBeans)
            .prepare();
    final var result = VadorBatch.validateAndFailFastForAny(recursiveBeans, validationConfig);
    assertThat(result).contains(UNKNOWN_EXCEPTION);
  }

  @Test
  void failFastForAnyWithPair() {
    final var batchValidationConfig =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .withValidatorEtrs(VALIDATOR_ETRS)
            .prepare();
    final var result =
        VadorBatch.validateAndFailFastForAny(VALIDATABLE_BATCH, Bean::getId, batchValidationConfig);
    assertThat(result).contains(Tuple.of(0, VALIDATION_FAILURE_1));
  }

  @Test
  void handleNullValidatablesByDefault() {
    // * NOTE 01/10/21 gopala.akshintala: Using vavr list as `java.util.List.of()` doesn't allow
    // `null`
    final var validatables =
        io.vavr.collection.List.of(new Bean(0), new Bean(1), null, new Bean(3), null).toJavaList();
    final var noOpConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate().prepare();
    final var results = VadorBatch.validateAndFailFastForEach(validatables, noOpConfig);
    assertEquals(results.size(), VALIDATABLE_BATCH.size());

    VavrAssertions.assertThat(results.get(0)).isRight().containsOnRight(new Bean(0));
    VavrAssertions.assertThat(results.get(1)).isRight().containsOnRight(new Bean(1));
    VavrAssertions.assertThat(results.get(2)).isLeft().containsOnLeft(null);
    VavrAssertions.assertThat(results.get(3)).isRight().containsOnRight(new Bean(3));
    VavrAssertions.assertThat(results.get(4)).isLeft().containsOnLeft(null);
  }

  @Test
  void handleNullValidatablesWithFailureForNullValidatable() {
    final var validatables =
        io.vavr.collection.List.of(new Bean(0), new Bean(1), null, new Bean(3), null).toJavaList();
    final var noOpConfig = BatchValidationConfig.<Bean, ValidationFailure>toValidate().prepare();
    final var results =
        VadorBatch.validateAndFailFastForEach(validatables, noOpConfig, NOTHING_TO_VALIDATE);
    assertEquals(results.size(), VALIDATABLE_BATCH.size());

    VavrAssertions.assertThat(results.get(0)).isRight().containsOnRight(new Bean(0));
    VavrAssertions.assertThat(results.get(1)).isRight().containsOnRight(new Bean(1));
    VavrAssertions.assertThat(results.get(2)).isLeft().containsOnLeft(NOTHING_TO_VALIDATE);
    VavrAssertions.assertThat(results.get(3)).isRight().containsOnRight(new Bean(3));
    VavrAssertions.assertThat(results.get(4)).isLeft().containsOnLeft(NOTHING_TO_VALIDATE);
  }

  @Test
  void errorAccumulateForValidators() {
    var predicateForValidId1 = (Predicate<Integer>) id -> id >= 2;
    var predicateForValidId2 = (Predicate<Integer>) id -> id <= 2;
    var predicateForValidId3 = (Predicate<Integer>) id -> id >= 1;
    List<Validator<Bean, ValidationFailure>> VALIDATORS =
        List.of(
            bean -> NONE,
            bean -> predicateForValidId1.test(bean.getId()) ? NONE : VALIDATION_FAILURE_1,
            bean -> predicateForValidId2.test(bean.getId()) ? NONE : VALIDATION_FAILURE_2,
            bean -> predicateForValidId3.test(bean.getId()) ? NONE : VALIDATION_FAILURE_3);
    final var result =
        VadorBatch.validateAndAccumulateErrors(
            VALIDATABLE_BATCH, VALIDATORS, NONE, throwable -> null);

    assertEquals(result.size(), VALIDATABLE_BATCH.size());
    assertTrue(result.stream().allMatch(r -> r.size() == VALIDATORS.size()));

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
                        && Either.right(new Bean(VALIDATABLE_BATCH.size() - 2 + index))
                            .equals(vf.get(1))));

    assertEquals(result.get(0).get(3), Either.left(VALIDATION_FAILURE_3));
  }

  @Value
  private static class Bean {
    int id;
  }

  @Value
  private static class RecursiveBean {
    int id;
    List<RecursiveBean> recursiveBeans;
  }
}
