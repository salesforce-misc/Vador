@file:JvmName("BatchRunner")

package org.revcloud.vader.runner

import io.vavr.Function1
import io.vavr.Tuple
import io.vavr.Tuple2
import io.vavr.control.Either
import org.revcloud.vader.lift.liftAllToEtr
import org.revcloud.vader.types.failures.FFBatchOfBatchFailure
import org.revcloud.vader.types.failures.FFBatchOfBatchFailureWithPair
import org.revcloud.vader.types.validators.Validator
import org.revcloud.vader.types.validators.ValidatorEtr
import java.util.Optional

fun <FailureT, ValidatableT> validateAndFailFastForEach(
  validatables: List<ValidatableT>,
  batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
  nullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?
): List<Either<FailureT?, ValidatableT?>> =
  failFastForEach(batchValidationConfig, nullValidatable, throwableMapper)(validatables)

fun <FailureT, ValidatableT, PairT> validateAndFailFastForEach(
  validatables: List<ValidatableT>,
  batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
  pairForInvalidMapper: (ValidatableT) -> PairT?,
  nullValidatable: FailureT,
  throwableMapper: (Throwable) -> FailureT?
): List<Either<Tuple2<PairT?, FailureT?>, ValidatableT?>> {
  val orderedValidationResults: List<Either<FailureT?, ValidatableT?>> =
    validateAndFailFastForEach(
      validatables,
      batchValidationConfig,
      nullValidatable,
      throwableMapper
    )
  return pairInvalidsWithIdentifier(
    orderedValidationResults,
    validatables,
    pairForInvalidMapper
  )
}

private fun <FailureT, ValidatableT, PairT> pairInvalidsWithIdentifier(
  orderedValidationResults: List<Either<FailureT, ValidatableT?>>,
  validatables: List<ValidatableT>,
  pairForInvalidMapper: (ValidatableT) -> PairT?
): List<Either<Tuple2<PairT?, FailureT>, ValidatableT?>> =
  orderedValidationResults.zip(validatables).map { (result, validatable) ->
    result.mapLeft { Tuple.of(pairForInvalidMapper(validatable), it) }
  }

fun <FailureT, ContainerValidatableT, MemberValidatableT, ContainerPairT, MemberPairT> validateAndFailFastForEach(
  validatables: List<ContainerValidatableT>,
  batchOfBatch1ValidationConfig: BatchOfBatch1ValidationConfig<ContainerValidatableT, MemberValidatableT, FailureT?>,
  containerPairForInvalidMapper: (ContainerValidatableT) -> ContainerPairT?,
  memberPairForInvalidMapper: (MemberValidatableT) -> MemberPairT?,
  nullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?
): List<Either<FFBatchOfBatchFailureWithPair<ContainerPairT?, MemberPairT?, FailureT?>, ContainerValidatableT?>> {
  val orderedValidationResults: List<Either<FFBatchOfBatchFailure<FailureT?>, ContainerValidatableT?>> =
    validateAndFailFastForEach(
      validatables,
      batchOfBatch1ValidationConfig,
      nullValidatable,
      throwableMapper
    )
  return pairInvalidsWithIdentifier(
    orderedValidationResults,
    validatables,
    batchOfBatch1ValidationConfig.withMemberBatchValidationConfig._1,
    containerPairForInvalidMapper,
    memberPairForInvalidMapper
  )
}

private fun <FailureT, ContainerValidatableT, MemberValidatableT, ContainerPairT, MemberPairT> pairInvalidsWithIdentifier(
  orderedValidationResults: List<Either<FFBatchOfBatchFailure<FailureT?>, ContainerValidatableT?>>,
  validatables: List<ContainerValidatableT>,
  memberBatchMapper: Function1<ContainerValidatableT, MutableCollection<MemberValidatableT>>,
  containerPairForInvalidMapper: (ContainerValidatableT) -> ContainerPairT?,
  memberPairForInvalidMapper: (MemberValidatableT) -> MemberPairT?,
): List<Either<FFBatchOfBatchFailureWithPair<ContainerPairT?, MemberPairT?, FailureT?>, ContainerValidatableT?>> =
  orderedValidationResults.zip(validatables)
    .map { (result, validatable) ->
      result.mapLeft {
        it.failure.bimap(
          { containerFailure -> Tuple.of(containerPairForInvalidMapper(validatable), containerFailure) },
          { memberFailures ->
            val members = memberBatchMapper.apply(validatable)
            members.zip(memberFailures)
              .map { (member, failure) -> Tuple.of(memberPairForInvalidMapper(member), failure) }
          }
        )
      }.mapLeft { FFBatchOfBatchFailureWithPair(it) }
    }

fun <FailureT, ContainerValidatableT, MemberValidatableT> validateAndFailFastForEach(
  validatables: List<ContainerValidatableT>,
  batchOfBatch1ValidationConfig: BatchOfBatch1ValidationConfig<ContainerValidatableT, MemberValidatableT, FailureT?>,
  nullValidatable: FailureT?,
  throwableMapper: (Throwable) -> FailureT?
): List<Either<FFBatchOfBatchFailure<FailureT?>, ContainerValidatableT?>> =
  failFastForEach(batchOfBatch1ValidationConfig, nullValidatable, throwableMapper)(validatables)

fun <FailureT, ValidatableT> validateAndFailFastForAny(
  validatables: List<ValidatableT>,
  batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
  nullValidatable: FailureT,
  throwableMapper: (Throwable) -> FailureT?
): Optional<FailureT> =
  failFastForAny(batchValidationConfig, nullValidatable, throwableMapper)(validatables)

/**
 * This returns the first failures of first invalid item in a batch and pairs it with an identifier using the `pairForInvalidMapper`
 * This can be used for `AllOrNone` scenarios in batch
 */
fun <FailureT, ValidatableT, PairT> validateAndFailFastForAny(
  validatables: List<ValidatableT>,
  batchValidationConfig: BatchValidationConfig<ValidatableT, FailureT?>,
  pairForInvalidMapper: (ValidatableT?) -> PairT?,
  nullValidatable: FailureT,
  throwableMapper: (Throwable) -> FailureT?
): Optional<Tuple2<PairT?, FailureT?>> =
  failFastForAny(batchValidationConfig, pairForInvalidMapper, nullValidatable, throwableMapper)(validatables)

// --- ERROR ACCUMULATION ---
// TODO 20/05/21 gopala.akshintala: ValidationConfig integration
/**
 * Validates a list of validatables against a list of Simple validations, in error-accumulation mode, per validatable.
 *
 * @param validatables
 * @param validators
 * @param invalidValidatable FailureT if the validatable is null.
 * @param <FailureT>
 * @param <ValidatableT>
 * @return List of Validation failures.
</ValidatableT></FailureT> */
fun <FailureT, ValidatableT> validateAndAccumulateErrors(
  validatables: List<ValidatableT>,
  validators: List<Validator<ValidatableT?, FailureT?>>,
  none: FailureT, // TODO 20/05/21 gopala.akshintala: Check on adding InvalidatableT
  throwableMapper: (Throwable) -> FailureT?,
): List<List<Either<FailureT?, ValidatableT?>>> =
  validateAndAccumulateErrors(validatables, liftAllToEtr(validators, none), throwableMapper)

/**
 * Validates a list of validatables against a list of validations, in error-accumulation mode, per validatable.
 *
 * @param validatables
 * @param validatorEtrs
 * @param invalidValidatable FailureT if the validatable is null.
 * @param <FailureT>
 * @param <ValidatableT>
 * @return List of Validation failures.
</ValidatableT></FailureT> */
fun <FailureT, ValidatableT> validateAndAccumulateErrors(
  validatables: List<ValidatableT>,
  validatorEtrs: List<ValidatorEtr<ValidatableT?, FailureT?>>,
  throwableMapper: (Throwable) -> FailureT?,
): List<List<Either<FailureT?, ValidatableT?>>> =
  validatables.map(accumulationStrategy(validatorEtrs, throwableMapper))
