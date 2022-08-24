/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

@file:JvmName("Utils")

package org.revcloud.vader.execution.util

import io.vavr.CheckedFunction1.liftTry
import io.vavr.Function1.identity
import io.vavr.Tuple
import io.vavr.Tuple2
import io.vavr.control.Either
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import org.revcloud.vader.config.FilterDuplicatesConfig.FilterDuplicatesConfigBuilder
import org.revcloud.vader.config.base.BaseContainerValidationConfig
import org.revcloud.vader.config.container.ContainerValidationConfig
import org.revcloud.vader.config.container.ContainerValidationConfigWith2Levels
import org.revcloud.vader.lift.liftAllToEtr
import org.revcloud.vader.lift.liftToEtr
import org.revcloud.vader.specs.component1
import org.revcloud.vader.specs.component2
import org.revcloud.vader.types.Validator
import org.revcloud.vader.types.ValidatorEtr
import java.util.Optional

// TODO 29/07/21 gopala.akshintala: Split this class into individual utils

@JvmSynthetic
internal fun <FailureT, ValidatableT> findFirstFailure(
  validatable: Either<FailureT?, ValidatableT?>,
  validators: Collection<ValidatorEtr<ValidatableT?, FailureT?>>,
  throwableMapper: (Throwable) -> FailureT?
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
  throwableMapper: (Throwable) -> FailureT?
): Sequence<Either<FailureT?, ValidatableT?>> =
  validatorEtrs.asSequence().map { fireValidator(validatable, it, throwableMapper) }

@JvmSynthetic
private fun <FailureT, ValidatableT> fireValidator(
  validatable: Either<FailureT?, ValidatableT?>,
  validatorEtr: ValidatorEtr<ValidatableT, FailureT>,
  throwableMapper: (Throwable) -> FailureT?
): Either<FailureT?, ValidatableT?> =
  liftTry(validatorEtr).apply(validatable)
    .fold({ left<FailureT?, ValidatableT?>(throwableMapper(it)) }) { it }
    .flatMap { validatable } // Put the original Validatable in the right state

@JvmSynthetic
internal fun <ContainerRootValidatableT, ContainerLevel1ValidatableT, FailureT : Any> validateBatchSize(
  container: ContainerRootValidatableT,
  containerValidationConfig: ContainerValidationConfigWith2Levels<ContainerRootValidatableT, ContainerLevel1ValidatableT, FailureT?>
): Optional<FailureT> {
  val containerLevel1Batch: Collection<ContainerLevel1ValidatableT> =
    containerValidationConfig.withBatchMembers.mapNotNull { it[container] }.flatten()
  return validateBatchSize(containerLevel1Batch, containerValidationConfig).or {
    val level2Batch: Collection<*> = containerLevel1Batch.mapNotNull { level1Container ->
      containerValidationConfig.withScopeOf1LevelDeep.withBatchMembers.mapNotNull { it[level1Container] }.flatten()
    }.flatten()
    validateBatchSize(level2Batch, containerValidationConfig.withScopeOf1LevelDeep)
  }
}

@JvmSynthetic
internal fun <ContainerT, FailureT : Any> validateBatchSize(
  container: ContainerT,
  containerValidationConfig: ContainerValidationConfig<ContainerT, FailureT?>
): Optional<FailureT> {
  val memberBatch: Collection<*> = containerValidationConfig.withBatchMembers.mapNotNull { it[container] }.flatten()
  return validateBatchSize(memberBatch, containerValidationConfig)
}

@JvmSynthetic
private fun <FailureT : Any> validateBatchSize(
  memberBatch: Collection<*>,
  containerConfig: BaseContainerValidationConfig<*, FailureT?>
): Optional<FailureT> {
  val minBatchSize = containerConfig.shouldHaveMinBatchSizeOrFailWith
  if (minBatchSize != null && memberBatch.size < minBatchSize._1) {
    return Optional.ofNullable(minBatchSize._2)
  }
  val maxBatchSize = containerConfig.shouldHaveMaxBatchSizeOrFailWith
  return if (maxBatchSize != null && memberBatch.size > maxBatchSize._1) {
    Optional.ofNullable(maxBatchSize._2)
  } else Optional.empty()
}

@JvmInline
internal value class Index(internal val index: Int)

internal fun <FailureT, ValidatableT> findAndFilterInvalids(
  validatables: Collection<ValidatableT>,
  failureForNullValidatable: FailureT?,
  filterConfigBuilders: Collection<FilterDuplicatesConfigBuilder<ValidatableT, FailureT?>>
): Collection<Either<FailureT?, ValidatableT?>> {
  val mapNullValidatables: Collection<Pair<ValidatableT?, Either<FailureT?, ValidatableT?>>> =
    validatables.map { if (it == null) Pair(null, left(failureForNullValidatable)) else Pair(it, right(it)) }
  return if (filterConfigBuilders.isEmpty()) {
    mapNullValidatables.map { it.second }
  } else {
    findAndFilterInvalids(
      mapNullValidatables.withIndex().map { Triple(Index(it.index), it.value.first, it.value.second) },
      filterConfigBuilders.iterator()
    ).sortedBy { it.first.index }.map { it.third }
  }
}

private tailrec fun <ValidatableT, FailureT> findAndFilterInvalids(
  validatables: List<Triple<Index, ValidatableT?, Either<FailureT?, ValidatableT?>>>,
  filterConfigs: Iterator<FilterDuplicatesConfigBuilder<ValidatableT, FailureT?>>
): List<Triple<Index, ValidatableT?, Either<FailureT?, ValidatableT?>>> =
  if (!filterConfigs.hasNext()) {
    validatables
  } else {
    val results = segregateNullAndDuplicateKeysInOrder(validatables, filterConfigs.next())
    findAndFilterInvalids(results, filterConfigs)
  }

private fun <ValidatableT, FailureT> segregateNullAndDuplicateKeysInOrder(
  validatables: List<Triple<Index, ValidatableT?, Either<FailureT?, ValidatableT?>>>,
  filterDuplicatesConfigBuilder: FilterDuplicatesConfigBuilder<ValidatableT, FailureT?>
): List<Triple<Index, ValidatableT?, Either<FailureT?, ValidatableT?>>> {
  val filterDuplicatesConfig = filterDuplicatesConfigBuilder.prepare()
  val duplicateFinder = filterDuplicatesConfig.findAndFilterDuplicatesWith
  val keyMapperForDuplicates = duplicateFinder ?: identity()

  val groups = validatables.groupBy { (_, validatable, _) -> Optional.ofNullable(validatable?.let { keyMapperForDuplicates.apply(it) }) }
  val withNullKeys = groups[Optional.empty()]
  val nullKeysWithFailures = associateValidatablesWithNullKeys(filterDuplicatesConfig.andFailNullKeysWith, withNullKeys)

  val (duplicates, nonDuplicates) = groups.filterKeys { it.isPresent }.values.partition { it.size > 1 }.toList().map { it.flatten() }
  val duplicatesWithFailures = associateValidatablesWithDuplicateKeys(filterDuplicatesConfig.andFailDuplicatesWith, duplicates)

  return duplicatesWithFailures + nullKeysWithFailures + nonDuplicates
}

private fun <FailureT, ValidatableT> associateValidatablesWithNullKeys(
  failureForNullKeys: FailureT?,
  withNullKeys: List<Triple<Index, ValidatableT?, Either<FailureT?, ValidatableT?>>>?
): List<Triple<Index, ValidatableT?, Either<FailureT?, ValidatableT?>>> =
  failureForNullKeys?.let {
    withNullKeys?.map(associateWithFailure(it))
  } ?: withNullKeys ?: emptyList()

private fun <FailureT, ValidatableT> associateValidatablesWithDuplicateKeys(
  failureForDuplicates: FailureT?,
  duplicates: List<Triple<Index, ValidatableT, Either<FailureT?, ValidatableT>>>
): List<Triple<Index, ValidatableT, Either<FailureT?, ValidatableT>>> =
  failureForDuplicates?.let {
    duplicates.map(associateWithFailure(it))
  } ?: emptyList()

private fun <FailureT, ValidatableT> associateWithFailure(failure: FailureT?) =
  { (index, validatable, validatableEtr): Triple<Index, ValidatableT, Either<FailureT?, ValidatableT>> ->
    when {
      validatableEtr.isLeft -> Triple(index, validatable, validatableEtr)
      else -> Triple(index, validatable, left(failure))
    }
  }

internal fun <ValidatableT, FailureT> findFirstInvalid(
  validatables: Collection<ValidatableT?>,
  failureForNullValidatable: FailureT?,
  filterDuplicatesConfigBuilders: Collection<FilterDuplicatesConfigBuilder<ValidatableT, FailureT?>>
): Optional<FailureT> =
  findFirstInvalid<ValidatableT, FailureT, Nothing>(
    validatables,
    filterDuplicatesConfigBuilders,
    failureForNullValidatable
  ).map { it._2 }

internal fun <ValidatableT, FailureT, PairT> findFirstInvalid(
  validatables: Collection<ValidatableT?>,
  filterDuplicatesConfigBuilders: Collection<FilterDuplicatesConfigBuilder<ValidatableT, FailureT?>>,
  failureForNullValidatable: FailureT? = null,
  pairForInvalidMapper: (ValidatableT?) -> PairT? = { null }
): Optional<Tuple2<PairT?, FailureT?>> =
  filterDuplicatesConfigBuilders.asSequence().map {
    findFirstInvalid(
      validatables,
      it,
      failureForNullValidatable,
      pairForInvalidMapper
    )
  }.find { it.isPresent } ?: Optional.empty()

private fun <ValidatableT, FailureT, PairT> findFirstInvalid(
  validatables: Collection<ValidatableT?>,
  filterDuplicatesConfigBuilder: FilterDuplicatesConfigBuilder<ValidatableT, FailureT?>,
  failureForNullValidatable: FailureT? = null,
  pairForInvalidMapper: (ValidatableT?) -> PairT? = { null }
): Optional<Tuple2<PairT?, FailureT?>> {
  if (validatables.isEmpty()) {
    return Optional.empty()
  } else if (validatables.size == 1) {
    val onlyValidatable = validatables.first()
    return if (onlyValidatable == null) Optional.ofNullable(Tuple.of(null, failureForNullValidatable)) else Optional.empty()
  }
  val filterDuplicatesConfig = filterDuplicatesConfigBuilder.prepare()
  val duplicateFinder = filterDuplicatesConfig.findAndFilterDuplicatesWith
  val keyMapperForDuplicates = duplicateFinder ?: identity()

  // Groups
  // null - Null Validatables
  // Optional.empty() - Validatables with Null keys
  // Optional[Key] - Validatables with Nonnull keys
  val groups = validatables.groupBy { if (it == null) null else Optional.ofNullable(keyMapperForDuplicates.apply(it)) }
  val nullValidatables = groups[null]
  if (nullValidatables != null && nullValidatables.isNotEmpty()) {
    return Optional.ofNullable(Tuple.of(null, failureForNullValidatable))
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

internal fun <ValidatableT, FailureT> fromValidators1(validators: Tuple2<out Collection<Validator<in ValidatableT?, FailureT?>>?, out FailureT?>?): List<ValidatorEtr<ValidatableT?, FailureT?>> =
  validators?.let { (validators, none) -> validators?.let { liftAllToEtr(it, none) } } ?: emptyList()

internal fun <ValidatableT, FailureT> fromValidators2(validators: Map<out Validator<in ValidatableT?, FailureT?>, FailureT?>): List<ValidatorEtr<ValidatableT?, FailureT?>> =
  validators.mapNotNull { (validator, none) -> liftToEtr(validator, none) }
