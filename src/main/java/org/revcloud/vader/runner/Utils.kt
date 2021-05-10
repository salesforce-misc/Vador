@file:JvmName("Utils")

package org.revcloud.vader.runner

import io.vavr.CheckedFunction1
import io.vavr.Function1
import io.vavr.Tuple
import io.vavr.Tuple2
import io.vavr.collection.Seq
import io.vavr.control.Either
import io.vavr.kotlin.left
import org.revcloud.vader.types.validators.Validator
import java.util.*
import java.util.function.Function
import java.util.stream.Stream

fun <FailureT, ValidatableT> fireValidators(
    validatable: Either<FailureT, ValidatableT>,
    validators: Stream<Validator<ValidatableT, FailureT>>,
    throwableMapper: Function1<Throwable, FailureT>
): Stream<Either<FailureT, ValidatableT>> =
    validators.map { currentValidator ->
        fireValidator(
            currentValidator,
            validatable,
            throwableMapper
        )
    }

fun <FailureT, ValidatableT> fireValidator(
    validator: Validator<ValidatableT, FailureT>,
    validatable: Either<FailureT, ValidatableT>,
    throwableMapper: Function1<Throwable, FailureT>
): Either<FailureT, ValidatableT> =
    CheckedFunction1.liftTry(validator).apply(validatable)
        .fold({ throwable: Throwable ->
            left<FailureT, ValidatableT>(
                throwableMapper.apply(throwable)
            )
        }, Function1.identity())
        .flatMap { validatable } // Put the original Validatable in the right state

fun <FailureT> validateSize(
    validatables: Collection<*>,
    headerConfig: HeaderValidationConfig<*, FailureT>
): Optional<FailureT> {
    val minBatchSize = headerConfig.shouldHaveMinBatchSize
    if (minBatchSize != null && validatables.size < minBatchSize._1) {
        return Optional.ofNullable(minBatchSize._2)
    }
    val maxBatchSize = headerConfig.shouldHaveMaxBatchSize
    return if (maxBatchSize != null && validatables.size > maxBatchSize._1) {
        Optional.ofNullable(maxBatchSize._2)
    } else Optional.empty()
}

fun <ValidatableT, FailureT> filterInvalidatablesAndDuplicates(
    validatables: List<ValidatableT>,
    invalidValidatable: FailureT,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT>
): Seq<Either<FailureT, ValidatableT>> {
    if (validatables.isEmpty()) {
        return io.vavr.collection.List.empty()
    } else if (validatables.size == 1) {
        val validatable: ValidatableT = validatables[0]
        return if (validatable == null) io.vavr.collection.List.of(Either.left(invalidValidatable)) else io.vavr.collection.List.of(
            Either.right(
                validatables[0]
            )
        )
    }
    val duplicateFinder = batchValidationConfig.findAndFilterDuplicatesWith
    val keyMapperForDuplicates = duplicateFinder ?: Function1.identity()
    val groups =
        io.vavr.collection.List.ofAll(validatables).zipWithIndex() // groups: invalids, duplicates, non-duplicates
            .groupBy { tuple2: Tuple2<ValidatableT, Int> ->
                if (tuple2._1 == null) null else Optional.ofNullable(
                    keyMapperForDuplicates.apply(tuple2._1)
                )
            }
    val invalids = groups[null]
        .map { nullValidatables: io.vavr.collection.List<Tuple2<ValidatableT, Int>> ->
            invalidate(
                nullValidatables,
                invalidValidatable
            )
        }
        .getOrElse(io.vavr.collection.List.empty())

    // TODO 11/04/21 gopala.akshintala: add test 
    if (duplicateFinder == null) { // Skip the rest if duplicateFinder is not defined
        val valids: Seq<Tuple2<Either<FailureT, ValidatableT>, Int>> = groups.remove(null).values().flatMap(
            Function1.identity()
        ).map(
            Function { tuple2: Tuple2<ValidatableT, Int> ->
                tuple2.map1 { right: ValidatableT ->
                    Either.right(
                        right
                    )
                }
            })
        return valids.appendAll(invalids).sortBy { obj: Tuple2<Either<FailureT, ValidatableT>, Int> -> obj._2() }
            .map { obj: Tuple2<Either<FailureT, ValidatableT>, Int> -> obj._1() }
    }
    val failureForNullKeys = batchValidationConfig.andFailNullKeysWith
    val withNullKeys = groups[Optional.empty()].getOrElse(io.vavr.collection.List.empty())
    val invalidsWithNullKeys = if (failureForNullKeys == null) withNullKeys.map { tuple2: Tuple2<ValidatableT, Int> ->
        tuple2.map1 { right: ValidatableT ->
            Either.right(
                right
            )
        }
    } else withNullKeys.map { tuple2: Tuple2<ValidatableT, Int> ->
        tuple2.map1 { ignore: ValidatableT ->
            Either.left<FailureT, ValidatableT>(
                failureForNullKeys
            )
        }
    }
    val partition = groups.removeAll(io.vavr.collection.List.of(null, Optional.empty())).values()
        .partition { group: io.vavr.collection.List<Tuple2<ValidatableT, Int>> -> group.size() == 1 }
    val failureForDuplicate = batchValidationConfig.andFailDuplicatesWith
    val duplicates: Seq<Tuple2<Either<FailureT, ValidatableT>, Int>> =
        if (failureForDuplicate == null) io.vavr.collection.List.empty() else partition._2!!.flatMap(
            Function1.identity()
        ).map { duplicate: Tuple2<ValidatableT, Int> ->
            Tuple.of(
                Either.left(failureForDuplicate),
                duplicate._2
            )
        }
    val nonDuplicates: Seq<Tuple2<Either<FailureT, ValidatableT>, Int>> =
        partition._1!!.flatMap(Function1.identity()).map(
            Function { tuple2: Tuple2<ValidatableT, Int> ->
                tuple2.map1 { right: ValidatableT ->
                    Either.right(
                        right
                    )
                }
            })
    return nonDuplicates.appendAll(duplicates).appendAll(invalids).appendAll(invalidsWithNullKeys)
        .sortBy { obj: Tuple2<Either<FailureT, ValidatableT>, Int> -> obj._2() }
        .map { obj: Tuple2<Either<FailureT, ValidatableT>, Int> -> obj._1() }
}

fun <ValidatableT, FailureT> filterInvalidatablesAndDuplicatesForAllOrNone(
    validatables: List<ValidatableT>,
    invalidValidatable: FailureT,
    batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT>
): Optional<FailureT> {
    if (validatables.isEmpty()) {
        return Optional.empty()
    } else if (validatables.size == 1) {
        val validatable: ValidatableT = validatables[0]
        return if (validatable == null) Optional.ofNullable(invalidValidatable) else Optional.empty()
    }
    val duplicateFinder = batchValidationConfig.findAndFilterDuplicatesWith
    val keyMapperForDuplicates = duplicateFinder ?: Function1.identity()
    val groups = io.vavr.collection.List.ofAll(validatables) // groups: invalids, duplicates, non-duplicates
        .groupBy { validatable: ValidatableT ->
            if (validatable == null) null else Optional.ofNullable(
                keyMapperForDuplicates.apply(validatable)
            )
        }
    val invalids = groups[null]
    if (invalids.isDefined && !invalids.get().isEmpty) {
        return Optional.ofNullable(invalidValidatable)
    }
    val invalidsWithNullKeys = groups[Optional.empty()]
    if (invalidsWithNullKeys.isDefined && !invalidsWithNullKeys.get().isEmpty) {
        return Optional.ofNullable(batchValidationConfig.andFailNullKeysWith)
    }
    val valids = groups.removeAll(io.vavr.collection.List.of(null, Optional.empty())).values()
    val failureForDuplicate = batchValidationConfig.andFailDuplicatesWith
    if (duplicateFinder != null && failureForDuplicate != null) {
        val partition = valids.partition { group: io.vavr.collection.List<ValidatableT> -> group.size() == 1 }
        if (!partition._2.isEmpty) {
            return Optional.of(failureForDuplicate)
        }
    }
    return Optional.empty()
}

private fun <FailureT, ValidatableT> invalidate(
    nullValidatables: Seq<Tuple2<ValidatableT, Int>>, invalidValidatable: FailureT
): Seq<Tuple2<Either<FailureT, ValidatableT>, Int>> =
    nullValidatables.map { nullValidatable: Tuple2<ValidatableT, Int> ->
        nullValidatable.map1 { ignore: ValidatableT ->
            Either.left(
                invalidValidatable
            )
        }
    }

internal fun <FailureT, ValidatableT> findFirstFailure(
    validatable: Either<FailureT, ValidatableT>,
    validationConfig: BaseValidationConfig<ValidatableT, FailureT>,
    throwableMapper: Function1<Throwable, FailureT>
): Either<FailureT, ValidatableT> =
    fireValidators(
        validatable,
        toValidators(validationConfig),
        throwableMapper
    ).filter { it.isLeft }
        .findFirst().orElse(validatable)

