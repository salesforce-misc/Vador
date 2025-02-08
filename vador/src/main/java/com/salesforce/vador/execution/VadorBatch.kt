/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
@file:JvmName("VadorBatch")

package com.salesforce.vador.execution

import com.salesforce.vador.config.BatchOfBatch1ValidationConfig
import com.salesforce.vador.config.BatchValidationConfig
import com.salesforce.vador.config.container.ContainerValidationConfig
import com.salesforce.vador.config.container.ContainerValidationConfigWith2Levels
import com.salesforce.vador.execution.strategies.accumulationStrategy
import com.salesforce.vador.execution.strategies.failFastForAny
import com.salesforce.vador.execution.strategies.failFastForAnyBatchOfBatch1
import com.salesforce.vador.execution.strategies.failFastForEach
import com.salesforce.vador.execution.strategies.failFastForEachBatchOfBatch1
import com.salesforce.vador.lift.liftAllToEtr
import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorEtr
import com.salesforce.vador.types.failures.FFABatchOfBatchFailureWithPair
import com.salesforce.vador.types.failures.FFEBatchOfBatchFailure
import com.salesforce.vador.types.failures.FFEBatchOfBatchFailureWithPair
import io.vavr.Function1
import io.vavr.Tuple
import io.vavr.Tuple2
import io.vavr.control.Either
import java.util.Optional

object VadorBatch {
  /** <--- CONTAINER --- */
  @JvmStatic
  @JvmOverloads
  fun <FailureT : Any, ContainerValidatableT> validateAndFailFastForContainer(
    batchValidatable: Collection<ContainerValidatableT>,
    containerValidationConfig: ContainerValidationConfig<ContainerValidatableT, FailureT?>,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): Optional<FailureT> =
    batchValidatable
      .asSequence()
      .map { container: ContainerValidatableT ->
        Vador.validateAndFailFastForContainer(container, containerValidationConfig, throwableMapper)
      }
      .firstOrNull { it.isPresent } ?: Optional.empty()

  @JvmStatic
  @JvmOverloads
  fun <FailureT : Any, ContainerValidatableT, PairT> validateAndFailFastForContainer(
    batchValidatable: Collection<ContainerValidatableT>,
    pairForInvalidMapper: (ContainerValidatableT?) -> PairT?,
    containerValidationConfig: ContainerValidationConfig<ContainerValidatableT, FailureT?>,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): Optional<Tuple2<PairT?, FailureT?>> =
    batchValidatable
      .asSequence()
      .map { container: ContainerValidatableT ->
        Vador.validateAndFailFastForContainer(container, containerValidationConfig, throwableMapper)
          .map { Tuple.of(pairForInvalidMapper(container), it) }
      }
      .firstOrNull { it.isPresent } ?: Optional.empty()

  /** For Container with 2 - Levels */
  @JvmStatic
  @JvmOverloads
  fun <
    FailureT : Any,
    ContainerValidatableT,
    NestedContainerValidatableT,
  > validateAndFailFastForContainer(
    batchValidatable: Collection<ContainerValidatableT>,
    containerValidationConfigWith2Levels:
      ContainerValidationConfigWith2Levels<
        ContainerValidatableT,
        NestedContainerValidatableT,
        FailureT?,
      >,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): Optional<FailureT> =
    batchValidatable
      .asSequence()
      .map { container: ContainerValidatableT ->
        Vador.validateAndFailFastForContainer(
          container,
          containerValidationConfigWith2Levels,
          throwableMapper,
        )
      }
      .firstOrNull { it.isPresent } ?: Optional.empty()

  @JvmStatic
  @JvmOverloads
  fun <
    FailureT : Any,
    ContainerValidatableT,
    NestedContainerValidatableT,
    PairT,
  > validateAndFailFastForContainer(
    batchValidatable: Collection<ContainerValidatableT>,
    pairForInvalidMapper: (ContainerValidatableT?) -> PairT?,
    containerValidationConfigWith2Levels:
      ContainerValidationConfigWith2Levels<
        ContainerValidatableT,
        NestedContainerValidatableT,
        FailureT?,
      >,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): Optional<Tuple2<PairT?, FailureT?>> =
    batchValidatable
      .asSequence()
      .map { container: ContainerValidatableT ->
        Vador.validateAndFailFastForContainer(
            container,
            containerValidationConfigWith2Levels,
            throwableMapper,
          )
          .map { Tuple.of(pairForInvalidMapper(container), it) }
      }
      .firstOrNull { it.isPresent } ?: Optional.empty()

  /** --- CONTAINER ---> */
  /** --- FOR EACH --- */
  @JvmStatic
  @JvmOverloads
  fun <FailureT, ValidatableT> validateAndFailFastForEach(
    validatables: List<ValidatableT?>,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
    failureForNullValidatable: FailureT? = null,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): List<Either<FailureT?, ValidatableT?>> =
    failFastForEach(batchValidationConfig, failureForNullValidatable, throwableMapper)(validatables)

  @JvmStatic
  @JvmOverloads
  fun <FailureT, ValidatableT, PairT> validateAndFailFastForEach(
    validatables: List<ValidatableT?>,
    pairForInvalidMapper: (ValidatableT?) -> PairT?,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
    failureForNullValidatable: FailureT? = null,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): List<Either<Tuple2<PairT?, FailureT?>, ValidatableT?>> {
    val orderedValidationResults: List<Either<FailureT?, ValidatableT?>> =
      validateAndFailFastForEach(
        validatables,
        batchValidationConfig,
        failureForNullValidatable,
        throwableMapper,
      )
    return pairInvalidsWithIdentifier(orderedValidationResults, validatables, pairForInvalidMapper)
  }

  private fun <FailureT, ValidatableT, PairT> pairInvalidsWithIdentifier(
    orderedValidationResults: List<Either<FailureT, ValidatableT?>>,
    validatables: List<ValidatableT>,
    pairForInvalidMapper: (ValidatableT) -> PairT?,
  ): List<Either<Tuple2<PairT?, FailureT>, ValidatableT?>> =
    orderedValidationResults.zip(validatables).map { (result, validatable) ->
      result.mapLeft { Tuple.of(pairForInvalidMapper(validatable), it) }
    }

  @JvmStatic
  @JvmOverloads
  fun <
    FailureT,
    ContainerValidatableT,
    MemberValidatableT,
    ContainerPairT,
    MemberPairT,
  > validateAndFailFastForEach(
    validatables: List<ContainerValidatableT>,
    batchOfBatch1ValidationConfig:
      BatchOfBatch1ValidationConfig<ContainerValidatableT, MemberValidatableT, FailureT?>,
    containerPairForInvalidMapper: (ContainerValidatableT?) -> ContainerPairT?,
    memberPairForInvalidMapper: (MemberValidatableT?) -> MemberPairT?,
    failureForNullValidatable: FailureT? = null,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): List<
    Either<
      FFEBatchOfBatchFailureWithPair<ContainerPairT?, MemberPairT?, FailureT?>,
      ContainerValidatableT?,
    >
  > {
    val orderedValidationResults:
      List<Either<FFEBatchOfBatchFailure<FailureT?>, ContainerValidatableT?>> =
      validateAndFailFastForEach(
        validatables,
        batchOfBatch1ValidationConfig,
        failureForNullValidatable,
        throwableMapper,
      )
    return pairInvalidsWithIdentifier(
      orderedValidationResults,
      validatables,
      batchOfBatch1ValidationConfig.withMemberBatchValidationConfig._1,
      containerPairForInvalidMapper,
      memberPairForInvalidMapper,
    )
  }

  private fun <
    FailureT,
    ContainerValidatableT,
    MemberValidatableT,
    ContainerPairT,
    MemberPairT,
  > pairInvalidsWithIdentifier(
    orderedValidationResults:
      List<Either<FFEBatchOfBatchFailure<FailureT?>, ContainerValidatableT?>>,
    validatables: List<ContainerValidatableT>,
    memberBatchMapper: Function1<ContainerValidatableT, Collection<MemberValidatableT>>,
    containerPairForInvalidMapper: (ContainerValidatableT) -> ContainerPairT?,
    memberPairForInvalidMapper: (MemberValidatableT) -> MemberPairT?,
  ): List<
    Either<
      FFEBatchOfBatchFailureWithPair<ContainerPairT?, MemberPairT?, FailureT?>,
      ContainerValidatableT?,
    >
  > =
    orderedValidationResults.zip(validatables).map { (result, validatable) ->
      result
        .mapLeft {
          it.failure.bimap(
            { containerFailure ->
              Tuple.of(containerPairForInvalidMapper(validatable), containerFailure)
            },
            { memberFailures ->
              val members = memberBatchMapper.apply(validatable)
              members.zip(memberFailures).map { (member, failure) ->
                Tuple.of(memberPairForInvalidMapper(member), failure)
              }
            },
          )
        }
        .mapLeft { FFEBatchOfBatchFailureWithPair(it) }
    }

  @JvmStatic
  @JvmOverloads
  fun <FailureT, ContainerValidatableT, MemberValidatableT> validateAndFailFastForEach(
    validatables: List<ContainerValidatableT?>,
    batchOfBatch1ValidationConfig:
      BatchOfBatch1ValidationConfig<ContainerValidatableT, MemberValidatableT, FailureT?>,
    failureForNullValidatable: FailureT? = null,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): List<Either<FFEBatchOfBatchFailure<FailureT?>, ContainerValidatableT?>> =
    failFastForEachBatchOfBatch1(
      batchOfBatch1ValidationConfig,
      failureForNullValidatable,
      throwableMapper,
    )(validatables)

  /** == FOR ANY == */
  @JvmStatic
  @JvmOverloads
  fun <FailureT, ValidatableT> validateAndFailFastForAny(
    validatables: List<ValidatableT?>,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
    failureForNullValidatable: FailureT? = null,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): Optional<FailureT> =
    failFastForAny(batchValidationConfig, failureForNullValidatable, throwableMapper)(validatables)

  /**
   * This returns the first failure of first invalid item in a batch and pairs it with an identifier
   * using the `pairForInvalidMapper` This can be used for `AllOrNone` scenarios in batch
   */
  @JvmStatic
  @JvmOverloads
  fun <FailureT, ValidatableT, PairT> validateAndFailFastForAny(
    validatables: List<ValidatableT?>,
    pairForInvalidMapper: (ValidatableT?) -> PairT?,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
    failureForNullValidatable: FailureT? = null,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): Optional<Tuple2<PairT?, FailureT?>> =
    failFastForAny(
      batchValidationConfig,
      failureForNullValidatable,
      throwableMapper,
      pairForInvalidMapper,
    )(validatables)

  @JvmStatic
  @JvmOverloads
  fun <FailureT, ContainerValidatableT, MemberValidatableT> validateAndFailFastForAny(
    validatables: List<ContainerValidatableT?>,
    batchOfBatch1ValidationConfig:
      BatchOfBatch1ValidationConfig<ContainerValidatableT, MemberValidatableT, FailureT?>,
    failureForNullValidatable: FailureT? = null,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): Optional<FailureT> =
    failFastForAnyBatchOfBatch1(
      batchOfBatch1ValidationConfig,
      failureForNullValidatable,
      throwableMapper,
    )(validatables)

  @JvmStatic
  @JvmOverloads
  fun <
    FailureT,
    ContainerValidatableT,
    MemberValidatableT,
    ContainerPairT,
    MemberPairT,
  > validateAndFailFastForAny(
    validatables: List<ContainerValidatableT?>,
    containerPairForInvalidMapper: (ContainerValidatableT?) -> ContainerPairT?,
    memberPairForInvalidMapper: (MemberValidatableT?) -> MemberPairT?,
    batchOfBatch1ValidationConfig:
      BatchOfBatch1ValidationConfig<ContainerValidatableT, MemberValidatableT, FailureT?>,
    failureForNullValidatable: FailureT? = null,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): Optional<FFABatchOfBatchFailureWithPair<ContainerPairT?, MemberPairT?, FailureT?>> =
    failFastForAnyBatchOfBatch1(
      batchOfBatch1ValidationConfig,
      failureForNullValidatable,
      throwableMapper,
      containerPairForInvalidMapper,
      memberPairForInvalidMapper,
    )(validatables)

  // --- ERROR ACCUMULATION ---
  // TODO 20/05/21 gopala.akshintala: Implement parity with Fail Fast
  /**
   * Validates a list of validatables against a list of Simple validations, in error-accumulation
   * mode, per validatable.
   *
   * @param validatables
   * @param validators
   * @param none Failure if the validatable is null.
   * @param <FailureT>
   * @param <ValidatableT>
   * @return List of Validation failures. </ValidatableT></FailureT>
   */
  @JvmStatic
  @JvmOverloads
  fun <FailureT, ValidatableT> validateAndAccumulateErrors(
    validatables: List<ValidatableT>,
    validators: List<Validator<ValidatableT?, FailureT?>>,
    none: FailureT, // ! TODO 27/09/21 gopala.akshintala: Remove this, like it's FF counterpart
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): List<List<Either<FailureT?, ValidatableT?>>> =
    validateAndAccumulateErrors(validatables, liftAllToEtr(validators, none), throwableMapper)

  /**
   * Validates a list of validatables against a list of validations, in error-accumulation mode, per
   * validatable.
   *
   * @param validatables
   * @param validatorEtrs
   * @param <FailureT>
   * @param <ValidatableT>
   * @return List of Validation failures. </ValidatableT></FailureT>
   */
  @JvmStatic
  @JvmOverloads
  fun <FailureT, ValidatableT> validateAndAccumulateErrors(
    validatables: List<ValidatableT>,
    validatorEtrs: List<ValidatorEtr<ValidatableT?, FailureT?>>,
    throwableMapper: (Throwable) -> FailureT? = { throw it },
  ): List<List<Either<FailureT?, ValidatableT?>>> =
    validatables.map(accumulationStrategy(validatorEtrs, throwableMapper))
}
