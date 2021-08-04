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
  validators: Collection<ValidatorEtr<ValidatableT?, FailureT?>>,
  throwableMapper: (Throwable) -> FailureT?,
): Either<FailureT?, ValidatableT?>? =
  if (validatable.isLeft) validatable
  else fireValidators(
    validatable,
    validators,
    throwableMapper
  ).firstOrNull { it.isLeft }

/**
 * To fire Collection of validators on a validatable.
 *
 * @return - Sequence of validator results
 */
@JvmSynthetic
internal fun <FailureT, ValidatableT> fireValidators(
  validatable: Either<FailureT?, ValidatableT?>,
  validatorEtrs: Collection<ValidatorEtr<ValidatableT, FailureT>>,
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
internal fun <ContainerRootValidatableT, ContainerLevel1ValidatableT, FailureT> validateBatchSize(
  container: ContainerRootValidatableT,
  containerValidationConfig: ContainerValidationConfigWith2Levels<ContainerRootValidatableT, ContainerLevel1ValidatableT, FailureT?>
): Optional<FailureT> {
  val containerLevel1Batch: Collection<ContainerLevel1ValidatableT> =
    containerValidationConfig.withBatchMappers.mapNotNull { it[container] }.flatten()
  return validateBatchSize(containerLevel1Batch, containerValidationConfig).or {
    val level2Batch: Collection<*> = containerLevel1Batch.mapNotNull { level1Container ->
      containerValidationConfig.withContainerLevel1ValidationConfig.withBatchMappers.mapNotNull { it[level1Container] }.flatten()
    }.flatten()
    validateBatchSize(level2Batch, containerValidationConfig.withContainerLevel1ValidationConfig)
  }
}

@JvmSynthetic
internal fun <ContainerT, FailureT> validateBatchSize(
  container: ContainerT,
  containerValidationConfig: ContainerValidationConfig<ContainerT, FailureT?>
): Optional<FailureT> {
  val memberBatch: Collection<*> = containerValidationConfig.withBatchMappers.mapNotNull { it[container] }.flatten()
  return validateBatchSize(memberBatch, containerValidationConfig)
}

@JvmSynthetic
private fun <FailureT> validateBatchSize(
  memberBatch: Collection<*>,
  containerConfig: BaseContainerValidationConfig<*, FailureT?>
): Optional<FailureT> {
  val minBatchSize = containerConfig.shouldHaveMinBatchSize
  if (minBatchSize != null && memberBatch.size < minBatchSize._1) {
    return Optional.ofNullable(minBatchSize._2)
  }
  val maxBatchSize = containerConfig.shouldHaveMaxBatchSize
  return if (maxBatchSize != null && memberBatch.size > maxBatchSize._1) {
    Optional.ofNullable(maxBatchSize._2)
  } else Optional.empty()
}

internal fun <FailureT, ValidatableT> findAndFilterInvalids(
  validatables: Collection<ValidatableT>,
  failureForNullValidatable: FailureT?,
  filterConfigs: Collection<FilterDuplicatesConfig<ValidatableT, FailureT?>>
): Collection<Either<FailureT?, ValidatableT?>> {
  val mapNullValidatables: Collection<Pair<ValidatableT?, Either<FailureT?, ValidatableT?>>> =
    validatables.map { if (it == null) Pair(null, left(failureForNullValidatable)) else Pair(it, right(it)) }
  return if (filterConfigs.isEmpty()) {
    mapNullValidatables.map { it.second }
  } else {
    findAndFilterInvalids(
      mapNullValidatables.withIndex().map { Triple(it.index, it.value.first, it.value.second) },
      filterConfigs.iterator()
    ).sortedBy { it.first }.map { it.third }
  }
}

private tailrec fun <ValidatableT, FailureT> findAndFilterInvalids(
  validatables: Collection<Triple<Int, ValidatableT?, Either<FailureT?, ValidatableT?>>>,
  filterConfigs: Iterator<FilterDuplicatesConfig<ValidatableT, FailureT?>>,
): Collection<Triple<Int, ValidatableT?, Either<FailureT?, ValidatableT?>>> =
  if (!filterConfigs.hasNext()) {
    validatables
  } else {
    val results = segregateNullAndDuplicateKeysInOrder(validatables, filterConfigs.next())
    findAndFilterInvalids(results, filterConfigs)
  }

private fun <ValidatableT, FailureT> segregateNullAndDuplicateKeysInOrder(
  validatables: Collection<Triple<Int, ValidatableT?, Either<FailureT?, ValidatableT?>>>,
  filterDuplicatesConfig: FilterDuplicatesConfig<ValidatableT, FailureT?>
): Collection<Triple<Int, ValidatableT?, Either<FailureT?, ValidatableT?>>> {
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
  groups: Map<Optional<Any>, Collection<Triple<Int, ValidatableT, Either<FailureT?, ValidatableT>>>>
): Collection<Triple<Int, ValidatableT, Either<FailureT?, ValidatableT>>> =
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

internal fun <ValidatableT, FailureT> findFirstInvalid(
  validatables: Collection<ValidatableT?>,
  nullValidatable: FailureT?,
  filterDuplicatesConfigs: Collection<FilterDuplicatesConfig<ValidatableT, FailureT?>>,
): Optional<FailureT> =
  findFirstInvalid<ValidatableT, FailureT, Nothing>(
    validatables,
    filterDuplicatesConfigs,
    nullValidatable
  ).map { it._2 }

internal fun <ValidatableT, FailureT, PairT> findFirstInvalid(
  validatables: Collection<ValidatableT?>,
  filterDuplicatesConfigs: Collection<FilterDuplicatesConfig<ValidatableT, FailureT?>>,
  nullValidatable: FailureT? = null,
  pairForInvalidMapper: (ValidatableT?) -> PairT? = { null }
): Optional<Tuple2<PairT?, FailureT?>> =
  filterDuplicatesConfigs.asSequence().map {
    findFirstInvalid(
      validatables,
      it,
      nullValidatable,
      pairForInvalidMapper
    )
  }.find { it.isPresent } ?: Optional.empty()

/**
 * This gives the result paired up with an identifier.
 */
internal fun <ValidatableT, FailureT, PairT> findFirstInvalid(
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
