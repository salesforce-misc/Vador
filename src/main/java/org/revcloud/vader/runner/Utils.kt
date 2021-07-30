@file:JvmName("Utils")

package org.revcloud.vader.runner

import io.vavr.CheckedFunction1.liftTry
import io.vavr.Function1.identity
import io.vavr.Tuple
import io.vavr.Tuple2
import io.vavr.control.Either
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import org.revcloud.vader.types.validators.ValidatorEtr
import java.util.Optional

// TODO 29/07/21 gopala.akshintala: Split this class into individual utils 

@JvmSynthetic
internal fun <FailureT, ValidatableT> findFirstFailure(
  validatable: Either<FailureT?, ValidatableT?>,
  validators: List<ValidatorEtr<ValidatableT?, FailureT?>>,
  throwableMapper: (Throwable) -> FailureT?,
): Either<FailureT?, ValidatableT?>? =
  if (validatable.isLeft) validatable
  else fireValidators(
    validatable,
    validators,
    throwableMapper
  ).firstOrNull { it.isLeft }

@JvmSynthetic
internal fun <FailureT, ValidatableT> fireValidators(
  validatable: Either<FailureT?, ValidatableT?>,
  validatorEtrs: List<ValidatorEtr<ValidatableT, FailureT>>,
  throwableMapper: (Throwable) -> FailureT?,
): Sequence<Either<FailureT?, ValidatableT?>> =
  validatorEtrs.asSequence().map { fireValidator(validatable, it, throwableMapper) }

@JvmSynthetic
internal fun <FailureT, ValidatableT> fireValidator(
  validatable: Either<FailureT?, ValidatableT?>,
  validatorEtr: ValidatorEtr<ValidatableT, FailureT>,
  throwableMapper: (Throwable) -> FailureT?,
): Either<FailureT?, ValidatableT?> =
  liftTry(validatorEtr).apply(validatable)
    .fold({ left<FailureT?, ValidatableT?>(throwableMapper(it)) }) { it }
    .flatMap { validatable } // Put the original Validatable in the right state

@JvmSynthetic
internal fun <FailureT> validateBatchSize(
  headerItems: Collection<*>,
  headerConfig: BaseHeaderValidationConfig<*, FailureT?>
): Optional<FailureT> {
  val minBatchSize = headerConfig.shouldHaveMinBatchSize
  if (minBatchSize != null && headerItems.size < minBatchSize._1) {
    return Optional.ofNullable(minBatchSize._2)
  }
  val maxBatchSize = headerConfig.shouldHaveMaxBatchSize
  return if (maxBatchSize != null && headerItems.size > maxBatchSize._1) {
    Optional.ofNullable(maxBatchSize._2)
  } else Optional.empty()
}

@JvmSynthetic
internal fun <FailureT> validateNestedBatchSize(
  headerItems: Collection<*>,
  headerConfig: HeaderValidationConfigWithNested<*, *, FailureT?>
): Optional<FailureT> {
  val minBatchSize = headerConfig.shouldHaveMinNestedBatchSize
  if (minBatchSize != null && headerItems.size < minBatchSize._1) {
    return Optional.ofNullable(minBatchSize._2)
  }
  val maxBatchSize = headerConfig.shouldHaveMaxNestedBatchSize
  return if (maxBatchSize != null && headerItems.size > maxBatchSize._1) {
    Optional.ofNullable(maxBatchSize._2)
  } else Optional.empty()
}

internal fun <FailureT, ValidatableT> findAndFilterDuplicates(
  validatables: Collection<ValidatableT>,
  failureForNullValidatable: FailureT?,
  filterConfigs: Collection<FilterDuplicatesConfig<ValidatableT, FailureT?>>
): List<Either<FailureT?, ValidatableT?>> {
  val mapNullValidatables: List<Pair<ValidatableT?, Either<FailureT?, ValidatableT?>>> =
    validatables.map { if (it == null) Pair(null, left(failureForNullValidatable)) else Pair(it, right(it)) }
  return if (filterConfigs.isEmpty()) {
    mapNullValidatables.map { it.second }
  } else {
    findAndFilterDuplicates(
      mapNullValidatables.withIndex().map { Triple(it.index, it.value.first, it.value.second) },
      filterConfigs.iterator()
    ).sortedBy { it.first }.map { it.third }
  }
}

private tailrec fun <ValidatableT, FailureT> findAndFilterDuplicates(
  validatables: List<Triple<Int, ValidatableT?, Either<FailureT?, ValidatableT?>>>,
  filterConfigs: Iterator<FilterDuplicatesConfig<ValidatableT, FailureT?>>,
): List<Triple<Int, ValidatableT?, Either<FailureT?, ValidatableT?>>> =
  if (!filterConfigs.hasNext()) {
    validatables
  } else {
    val results = segregateNullAndDuplicateKeysInOrder(validatables, filterConfigs.next())
    findAndFilterDuplicates(results, filterConfigs)
  }

private fun <ValidatableT, FailureT> segregateNullAndDuplicateKeysInOrder(
  validatables: List<Triple<Int, ValidatableT?, Either<FailureT?, ValidatableT?>>>,
  filterDuplicatesConfig: FilterDuplicatesConfig<ValidatableT, FailureT?>
): List<Triple<Int, ValidatableT?, Either<FailureT?, ValidatableT?>>> {
  val duplicateFinder = filterDuplicatesConfig.findAndFilterDuplicatesWith
  val keyMapperForDuplicates = duplicateFinder ?: identity()

  val groups =
    validatables.groupBy { (_, validatable, _) -> Optional.ofNullable(validatable?.let { keyMapperForDuplicates.apply(it) }) }
  val withNullKeys = mapValidatablesWithNullKeys(filterDuplicatesConfig.andFailNullKeysWith, groups)

  val partition = groups.filterKeys { it.isPresent }.values.partition { it.size > 1 }
  val duplicates = mapValidatablesWithDuplicateKeys(filterDuplicatesConfig.andFailDuplicatesWith, partition.first)

  val nonDuplicates = partition.second.flatten()
  return duplicates + withNullKeys + nonDuplicates
}

private fun <FailureT, ValidatableT> mapValidatablesWithNullKeys(
  failureForNullKeys: FailureT?,
  groups: Map<Optional<Any>, List<Triple<Int, ValidatableT, Either<FailureT?, ValidatableT>>>>
): List<Triple<Int, ValidatableT, Either<FailureT?, ValidatableT>>> =
  failureForNullKeys?.let {
    groups[Optional.empty()]?.map(mapWithFailure(it))
  } ?: groups[Optional.empty()] ?: emptyList()

private fun <FailureT, ValidatableT> mapValidatablesWithDuplicateKeys(
  failureForDuplicates: FailureT?,
  duplicates: List<List<Triple<Int, ValidatableT, Either<FailureT?, ValidatableT>>>>,
): List<Triple<Int, ValidatableT, Either<FailureT?, ValidatableT>>> =
  failureForDuplicates?.let {
    duplicates.flatten().map(mapWithFailure(it))
  } ?: emptyList()

private fun <FailureT, ValidatableT> mapWithFailure(failure: FailureT?) =
  { (index, validatable, validatableEtr): Triple<Int, ValidatableT, Either<FailureT?, ValidatableT>> ->
    when {
      validatableEtr.isLeft -> Triple(index, validatable, validatableEtr)
      else -> Triple(index, validatable, left(failure))
    }
  }

internal fun <ValidatableT, FailureT> findFistNullValidatableOrDuplicate(
  validatables: Collection<ValidatableT?>,
  nullValidatable: FailureT?,
  filterDuplicatesConfigs: Collection<FilterDuplicatesConfig<ValidatableT, FailureT?>>,
): Optional<FailureT> =
  findFistNullValidatableOrDuplicate<ValidatableT, FailureT, Nothing>(
    validatables,
    filterDuplicatesConfigs,
    nullValidatable
  ).map { it._2 }

internal fun <ValidatableT, FailureT, PairT> findFistNullValidatableOrDuplicate(
  validatables: Collection<ValidatableT?>,
  filterDuplicatesConfigs: Collection<FilterDuplicatesConfig<ValidatableT, FailureT?>>,
  nullValidatable: FailureT? = null,
  pairForInvalidMapper: (ValidatableT?) -> PairT? = { null }
): Optional<Tuple2<PairT?, FailureT?>> =
  filterDuplicatesConfigs.asSequence().map {
    findFistNullValidatableOrDuplicate(
      validatables,
      it,
      nullValidatable,
      pairForInvalidMapper
    )
  }.find { it.isPresent } ?: Optional.empty()

/**
 * This gives the result paired up with an identifier.
 */
internal fun <ValidatableT, FailureT, PairT> findFistNullValidatableOrDuplicate(
  validatables: Collection<ValidatableT?>,
  filterDuplicatesConfig: FilterDuplicatesConfig<ValidatableT, FailureT?>,
  nullValidatable: FailureT? = null,
  pairForInvalidMapper: (ValidatableT?) -> PairT? = { null }
): Optional<Tuple2<PairT?, FailureT?>> {
  if (validatables.isEmpty()) {
    return Optional.empty()
  } else if (validatables.size == 1) {
    val onlyValidatable = validatables.first()
    return if (onlyValidatable == null) Optional.ofNullable(Tuple.of(null, nullValidatable)) else Optional.empty()
  }
  val duplicateFinder = filterDuplicatesConfig.findAndFilterDuplicatesWith
  val keyMapperForDuplicates = duplicateFinder ?: identity()

  // Groups
  // null - Null Validatables
  // Optional.empty() - Validatables with Null keys
  // Optional[Key] - Validatables with Nonnull keys
  val groups = validatables.groupBy { if (it == null) null else Optional.ofNullable(keyMapperForDuplicates.apply(it)) }
  val nullValidatables = groups[null]
  if (nullValidatables != null && nullValidatables.isNotEmpty()) {
    return Optional.ofNullable(Tuple.of(null, nullValidatable))
  }
  val invalidsWithNullKeys = groups[Optional.empty()]
  if (!invalidsWithNullKeys.isNullOrEmpty()) {
    return Optional.ofNullable(
      Tuple.of(
        pairForInvalidMapper(invalidsWithNullKeys.first()),
        filterDuplicatesConfig.andFailNullKeysWith
      )
    )
  }
  val failureForDuplicate = filterDuplicatesConfig.andFailDuplicatesWith
  if (duplicateFinder != null && failureForDuplicate != null) {
    val remaining = groups.filterKeys { it != null && it.isPresent }.values
    return remaining.find { it.size > 1 }
      ?.let { Optional.of(Tuple.of(pairForInvalidMapper(it.first()), failureForDuplicate)) } ?: Optional.empty()
  }
  return Optional.empty()
}
