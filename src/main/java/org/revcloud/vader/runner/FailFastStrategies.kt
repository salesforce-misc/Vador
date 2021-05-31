package org.revcloud.vader.runner

import io.vavr.control.Either
import io.vavr.kotlin.right
import java.util.Optional

internal typealias FailFast<ValidatableT, FailureT> = (ValidatableT) -> Optional<FailureT>

internal typealias FailFastForEach<ValidatableT, FailureT> = (List<ValidatableT>) -> List<Either<FailureT?, ValidatableT?>>

internal typealias FailFastForAll<ValidatableT, FailureT> = (List<ValidatableT>) -> Optional<FailureT>

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
  validationConfig: ValidationConfig<ValidatableT, FailureT>,
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
</ValidatableT></FailureT> */
@JvmSynthetic
internal fun <FailureT, ValidatableT> failFastForEach(
  validationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
  nullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?
): FailFastForEach<ValidatableT, FailureT> = { validatables: List<ValidatableT> ->
  val filteredValidatables = handleNullValidatablesAndDuplicates(validatables, nullValidatable, validationConfig)
  filteredValidatables.map { findFirstFailure(it, toValidators(validationConfig), throwableMapper) ?: it }
}

// TODO 13/05/21 gopala.akshintala: Reconsider any advantage of having this as a HOF 
@JvmSynthetic
internal fun <FailureT, ValidatableT> failFastForAll(
  invalidValidatable: FailureT,
  batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
  throwableMapper: (Throwable) -> FailureT?
): FailFastForAll<ValidatableT, FailureT> = { validatables ->
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
