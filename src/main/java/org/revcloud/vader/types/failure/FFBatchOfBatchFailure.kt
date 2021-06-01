package org.revcloud.vader.types.failure

import io.vavr.control.Either

/**
 * This is an inline wrapper to represent `FFBatchOfBatchFailure`
 */
@JvmInline
value class FFBatchOfBatchFailure<FailureT>(val failure: Either<FailureT?, List<FailureT?>>) {
  fun getContainerFailure(): FailureT? = if (failure.isLeft) failure.left else null
  fun getBatchMemberFailures(): List<FailureT?> = if (failure.isRight) failure.get() else emptyList()
}
