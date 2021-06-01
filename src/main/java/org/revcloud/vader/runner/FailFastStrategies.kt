package org.revcloud.vader.runner

import io.vavr.control.Either
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import org.revcloud.vader.types.failure.FFBatchOfBatchFailure
import java.util.Optional

internal typealias FailFast<ValidatableT, FailureT> = (ValidatableT) -> Optional<FailureT>

internal typealias FailFastForEach<ValidatableT, FailureT> = (Collection<ValidatableT>) -> List<Either<FailureT?, ValidatableT?>>

internal typealias FailFastForEachNestedBatch1<ValidatableT, FailureT> = (Collection<ValidatableT>) -> List<Either<FFBatchOfBatchFailure<FailureT?>, ValidatableT?>>

internal typealias FailFastForAny<ValidatableT, FailureT> = (Collection<ValidatableT>) -> Optional<FailureT>

internal typealias FailFastForHeader<ValidatableT, FailureT> = (ValidatableT) -> Optional<FailureT>

/**
 * Config
 *
 * @param invalidValidatable
 * @param throwableMapper
 * @param validationConfig
 * @param <FailureT>
 * @param <ValidatableT>
 * @return
 */
@JvmSynthetic
internal fun <FailureT, ValidatableT> failFast(
  validationConfig: ValidationConfig<ValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): FailFast<ValidatableT, FailureT> = { validatable: ValidatableT ->
  findFirstFailure(right(validatable), toValidators(validationConfig), throwableMapper)
    .toFailureOptional()
}

/**
 * Batch + Simple + Config
 *
 * @param nullValidatable
 * @param throwableMapper
 * @param validationConfig
 * @param <FailureT>
 * @param <ValidatableT>
 * @return
 */
@JvmSynthetic
internal fun <FailureT, ValidatableT> failFastForEach(
  validationConfig: BaseBatchValidationConfig<ValidatableT, FailureT?>,
  nullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?
): FailFastForEach<ValidatableT, FailureT> = { validatables: Collection<ValidatableT> ->
  handleNullValidatablesAndDuplicates(validatables, nullValidatable, validationConfig)
    .map { findFirstFailure(it, toValidators(validationConfig), throwableMapper) ?: it }
}

@JvmSynthetic
internal fun <FailureT, ContainerValidatableT, MemberValidatableT> failFastForEach(
  batchOfBatch1ValidationConfig: BatchOfBatch1ValidationConfig<ContainerValidatableT, MemberValidatableT, FailureT?>,
  nullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?
): FailFastForEachNestedBatch1<ContainerValidatableT, FailureT> = { validatables: Collection<ContainerValidatableT> ->
  handleNullValidatablesAndDuplicates(validatables, nullValidatable, batchOfBatch1ValidationConfig)
    .map { containerValidatable: Either<FailureT?, ContainerValidatableT?> ->
      findFirstFailure(
        containerValidatable,
        toValidators(batchOfBatch1ValidationConfig),
        throwableMapper
      ) ?: containerValidatable
    }
    .map { validContainer: Either<FailureT?, ContainerValidatableT?> ->
      validContainer
        .map(batchOfBatch1ValidationConfig.withMemberBatchValidationConfig._1)
        .map { members: Collection<MemberValidatableT> ->
          failFastForEach(
            batchOfBatch1ValidationConfig.withMemberBatchValidationConfig._2,
            nullValidatable,
            throwableMapper
          )(members)
        }
        .map { memberResults: List<Either<FailureT?, MemberValidatableT?>> -> memberResults.map { it.flatMap { validContainer } } }
    }
    .map { result: Either<FailureT?, List<Either<FailureT?, ContainerValidatableT?>>> ->
      result.fold<Either<Either<FailureT?, List<FailureT?>>, ContainerValidatableT?>?>(
        { containerFailure -> left(left(containerFailure)) },
        { memberResults ->
          val memberFailures = memberResults.filter { it.isLeft }
          if (memberFailures.isEmpty()) right(memberFailures.firstOrNull()?.get())
          else left(right(memberFailures.map { it.left }))
        }
      ).mapLeft { FFBatchOfBatchFailure(it) }
    }
}

// TODO 13/05/21 gopala.akshintala: Reconsider any advantage of having this as a HOF 
@JvmSynthetic
internal fun <FailureT, ValidatableT> failFastForAny(
  invalidValidatable: FailureT,
  batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): FailFastForAny<ValidatableT, FailureT> = { validatables ->
  findFistNullValidatableOrDuplicate(
    validatables,
    invalidValidatable,
    batchValidationConfig
  ).or {
    validatables.map {
      val validatable = right<FailureT?, ValidatableT?>(it)
      findFirstFailure(validatable, toValidators(batchValidationConfig), throwableMapper) ?: validatable
    }.firstOrNull { it.isLeft }.toFailureOptional()
  }
}

@JvmSynthetic
internal fun <FailureT, ValidatableT> failFastForHeader(
  validationConfig: HeaderValidationConfig<ValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): FailFastForHeader<ValidatableT, FailureT> = { validatable: ValidatableT ->
  val batch: List<*> = validationConfig.withBatchMappers.mapNotNull { it[validatable] }.flatten()
  validateBatchSize(batch, validationConfig).or {
    findFirstFailure(right(validatable), validationConfig.headerValidators, throwableMapper)
      .toFailureOptional()
  }
}
