/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.execution.strategies

import com.salesforce.vador.config.BatchOfBatch1ValidationConfig
import com.salesforce.vador.config.ValidationConfig
import com.salesforce.vador.config.base.BaseBatchValidationConfig
import com.salesforce.vador.config.base.BaseValidationConfig
import com.salesforce.vador.config.container.ContainerValidationConfig
import com.salesforce.vador.config.container.ContainerValidationConfigWith2Levels
import com.salesforce.vador.execution.strategies.util.configToValidators
import com.salesforce.vador.execution.strategies.util.findAndFilterInvalids
import com.salesforce.vador.execution.strategies.util.findFirstFailure
import com.salesforce.vador.execution.strategies.util.findFirstInvalid
import com.salesforce.vador.execution.strategies.util.validateBatchSize
import com.salesforce.vador.types.ValidatorEtr
import com.salesforce.vador.types.failures.FFABatchOfBatchFailureWithPair
import com.salesforce.vador.types.failures.FFEBatchOfBatchFailure
import io.vavr.Tuple
import io.vavr.Tuple2
import io.vavr.control.Either
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import java.util.*

internal typealias FailFastForContainer<ValidatableT, FailureT> =
  (ValidatableT) -> Optional<FailureT>

internal typealias FailFast<ValidatableT, FailureT> = (ValidatableT) -> Optional<FailureT>

internal typealias FailFastForEach<ValidatableT, FailureT> =
  (Collection<ValidatableT?>) -> List<Either<FailureT?, ValidatableT?>>

internal typealias FailFastForEachBatchOfBatch1<ValidatableT, FailureT> =
  (Collection<ValidatableT?>) -> List<Either<FFEBatchOfBatchFailure<FailureT?>, ValidatableT?>>

internal typealias FailFastForAny<ValidatableT, FailureT> =
  (Collection<ValidatableT?>) -> Optional<FailureT>

internal typealias FailFastForAnyWithPair<ValidatableT, FailureT, PairT> =
  (Collection<ValidatableT?>) -> Optional<Tuple2<PairT?, FailureT?>>

internal typealias FailFastForAnyBatchOfBatch1WithPair<
  ValidatableT, FailureT, ContainerPairT, MemberPairT> =
  (Collection<ValidatableT?>) -> Optional<
      FFABatchOfBatchFailureWithPair<ContainerPairT?, MemberPairT?, FailureT?>,
    >

@JvmSynthetic
internal fun <ValidatableT, FailureT : Any> failFast(
  validationConfig: ValidationConfig<ValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): FailFast<ValidatableT, FailureT> = { validatable: ValidatableT ->
  findFirstFailureRecursively(validatable, validationConfig, throwableMapper).toFailureOptional()
}

private fun <ValidatableT, FailureT : Any> findFirstFailureRecursively(
  validatable: ValidatableT,
  validationConfig: BaseValidationConfig<ValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): Either<FailureT?, ValidatableT?>? {
  val validatorsFromAnnotationNew: List<ValidatorEtr<ValidatableT?, FailureT?>> =
    AnnotationProcessor.deriveValidators(validatable, validationConfig.forAnnotations)
  // combined list is a list of validators and annotation validators
  val combinedListOfValidators: List<ValidatorEtr<ValidatableT?, FailureT?>> =
    validatorsFromAnnotationNew + configToValidators(validationConfig)
  return findFirstFailure(right(validatable), combinedListOfValidators, throwableMapper)
    ?: validationConfig.withRecursiveMapper
      ?.apply(validatable)
      ?.asSequence()
      ?.map { findFirstFailureRecursively(it, validationConfig, throwableMapper) }
      ?.filterNotNull()
      ?.firstOrNull()
}

/**
 * Batch + Simple + Config
 *
 * @param failureForNullValidatable
 * @param throwableMapper
 * @param batchValidationConfig
 * @param <FailureT>
 * @param <ValidatableT>
 * @return
 */
@JvmSynthetic
internal fun <FailureT, ValidatableT> failFastForEach(
  batchValidationConfig: BaseBatchValidationConfig<ValidatableT, FailureT?>,
  failureForNullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?
): FailFastForEach<ValidatableT, FailureT> = { validatables: Collection<ValidatableT?> ->
  findAndFilterInvalids(
      validatables,
      failureForNullValidatable,
      batchValidationConfig.findAndFilterDuplicatesConfigs
    )
    .map { findFirstFailure(it, configToValidators(batchValidationConfig), throwableMapper) ?: it }
}

@JvmSynthetic
internal fun <ContainerValidatableT, MemberValidatableT, FailureT> failFastForEachBatchOfBatch1(
  batchOfBatch1ValidationConfig:
    BatchOfBatch1ValidationConfig<ContainerValidatableT, MemberValidatableT, FailureT?>,
  failureForNullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?
): FailFastForEachBatchOfBatch1<ContainerValidatableT, FailureT> =
  { containerValidatables: Collection<ContainerValidatableT?> ->
    failFastForEach(batchOfBatch1ValidationConfig, failureForNullValidatable, throwableMapper)(
        containerValidatables
      )
      .map { validContainer: Either<FailureT?, ContainerValidatableT?> ->
        validContainer
          .map(batchOfBatch1ValidationConfig.withMemberBatchValidationConfig._1)
          .map { members: Collection<MemberValidatableT> ->
            failFastForEach(
              batchOfBatch1ValidationConfig.withMemberBatchValidationConfig._2,
              failureForNullValidatable,
              throwableMapper
            )(members)
          }
          .map { memberResults: List<Either<FailureT?, MemberValidatableT?>> ->
            memberResults.map { it.flatMap { validContainer } }
          }
      }
      .map { result: Either<FailureT?, List<Either<FailureT?, ContainerValidatableT?>>> ->
        result
          .fold<Either<Either<FailureT?, List<FailureT?>>, ContainerValidatableT?>?>(
            { containerFailure -> left(left(containerFailure)) },
            { memberResults ->
              val memberFailures = memberResults.filter { it.isLeft }
              if (memberFailures.isEmpty()) {
                right(memberFailures.firstOrNull()?.get())
              } else {
                left(right(memberFailures.map { it.left }))
              }
            }
          )
          .mapLeft { FFEBatchOfBatchFailure(it) }
      }
  }

@JvmSynthetic
internal fun <ContainerValidatableT, MemberValidatableT, FailureT> failFastForAnyBatchOfBatch1(
  batchOfBatch1ValidationConfig:
    BatchOfBatch1ValidationConfig<ContainerValidatableT, MemberValidatableT, FailureT?>,
  failureForNullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?
): FailFastForAny<ContainerValidatableT, FailureT> =
  { containerValidatables: Collection<ContainerValidatableT?> ->
    failFastForAnyBatchOfBatch1<
        ContainerValidatableT, MemberValidatableT, FailureT, Nothing, Nothing
      >(
        batchOfBatch1ValidationConfig,
        failureForNullValidatable,
        throwableMapper
      )(containerValidatables)
      .flatMap { it.containerFailure.or { it.batchMemberFailure } }
      .map { it?._2 }
  }

@JvmSynthetic
internal fun <
  ContainerValidatableT,
  MemberValidatableT,
  FailureT,
  ContainerPairT,
  MemberPairT> failFastForAnyBatchOfBatch1(
  batchOfBatch1ValidationConfig:
    BatchOfBatch1ValidationConfig<ContainerValidatableT, MemberValidatableT, FailureT?>,
  failureForNullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?,
  containerPairForInvalidMapper: (ContainerValidatableT?) -> ContainerPairT? = { null },
  memberPairForInvalidMapper: (MemberValidatableT?) -> MemberPairT? = { null }
): FailFastForAnyBatchOfBatch1WithPair<
  ContainerValidatableT, FailureT, ContainerPairT, MemberPairT
> = { containerValidatables: Collection<ContainerValidatableT?> ->
  failFastForAny(
      batchOfBatch1ValidationConfig,
      failureForNullValidatable,
      throwableMapper,
      containerPairForInvalidMapper
    )(containerValidatables)
    .map { FFABatchOfBatchFailureWithPair<ContainerPairT?, MemberPairT?, FailureT?>(left(it)) }
    .or {
      containerValidatables
        .asSequence()
        .map { batchOfBatch1ValidationConfig.withMemberBatchValidationConfig._1.apply(it) }
        .map { members: Collection<MemberValidatableT> ->
          failFastForAny(
              batchOfBatch1ValidationConfig.withMemberBatchValidationConfig._2,
              failureForNullValidatable,
              throwableMapper,
              memberPairForInvalidMapper
            )(members)
            .map {
              FFABatchOfBatchFailureWithPair<ContainerPairT?, MemberPairT?, FailureT?>(right(it))
            }
        }
        .firstOrNull { it.isPresent }
        ?: Optional.empty()
    }
}

// TODO 13/05/21 gopala.akshintala: Reconsider any advantage of having this as a HOF
@JvmSynthetic
internal fun <ValidatableT, FailureT> failFastForAny(
  batchValidationConfig: BaseBatchValidationConfig<ValidatableT, FailureT?>,
  failureForNullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?
): FailFastForAny<ValidatableT, FailureT> = { validatables ->
  failFastForAny<ValidatableT, FailureT, Nothing>(
      batchValidationConfig,
      failureForNullValidatable,
      throwableMapper
    )(validatables)
    .map { it._2 }
}

@JvmSynthetic
internal fun <ValidatableT, FailureT, PairT> failFastForAny(
  batchValidationConfig: BaseBatchValidationConfig<ValidatableT, FailureT?>,
  failureForNullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?,
  pairForInvalidMapper: (ValidatableT?) -> PairT? = { null }
): FailFastForAnyWithPair<ValidatableT, FailureT, PairT> = { validatables ->
  findFirstInvalid(
      validatables,
      batchValidationConfig.findAndFilterDuplicatesConfigs,
      failureForNullValidatable,
      pairForInvalidMapper
    )
    .or {
      validatables
        .asSequence()
        .map { validatable ->
          findFirstFailureRecursively(validatable, batchValidationConfig, throwableMapper)
            ?.mapLeft { failure -> Tuple.of(pairForInvalidMapper(validatable), failure) }
        }
        .firstOrNull { it?.isLeft == true }
        .toFailureWithPairOptional()
    }
}

@JvmSynthetic
internal fun <ContainerValidatableT, FailureT : Any> failFastForContainer(
  containerValidationConfig: ContainerValidationConfig<ContainerValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): FailFastForContainer<ContainerValidatableT, FailureT> = { container: ContainerValidatableT ->
  validateBatchSize(container, containerValidationConfig).or {
    findFirstFailure(
        right(container),
        containerValidationConfig.containerValidators,
        throwableMapper
      )
      .toFailureOptional()
  }
}

@JvmSynthetic
internal fun <
  ContainerValidatableT, NestedContainerValidatableT, FailureT : Any> failFastForContainer(
  containerValidationConfigWith2Levels:
    ContainerValidationConfigWith2Levels<
      ContainerValidatableT, NestedContainerValidatableT, FailureT?
    >,
  throwableMapper: (Throwable) -> FailureT?
): FailFastForContainer<ContainerValidatableT, FailureT> = { container: ContainerValidatableT ->
  validateBatchSize(container, containerValidationConfigWith2Levels).or {
    findFirstFailure(
        right(container),
        containerValidationConfigWith2Levels.containerValidators,
        throwableMapper
      )
      .toFailureOptional()
  }
}

private fun <FailureT : Any> Either<FailureT?, *>?.toFailureOptional(): Optional<FailureT> {
  val swapped = this?.swap() ?: return Optional.empty()
  return if (swapped.isEmpty) Optional.empty() else Optional.ofNullable(swapped.get())
}

private fun <FailureT, PairT> Either<Tuple2<PairT?, FailureT?>, *>?.toFailureWithPairOptional():
  Optional<Tuple2<PairT?, FailureT?>> {
  val swapped = this?.swap() ?: return Optional.empty()
  return if (swapped.isEmpty) Optional.empty() else Optional.ofNullable(swapped.get())
}
