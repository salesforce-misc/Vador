package org.revcloud.vader.types.failures

import io.vavr.control.Either

/**
 * This is an wrapper to represent `FFEBatchOfBatchFailure`
 */
class FFEBatchOfBatchFailure<FailureT>(val failure: Either<FailureT?, List<FailureT?>>) : Either<FailureT?, List<FailureT?>> by failure {
  val containerFailure: FailureT?
    get() = if (failure.isLeft) failure.left else null

  val batchMemberFailures: List<FailureT?>
    get() = if (failure.isRight) failure.get() else emptyList()
}
