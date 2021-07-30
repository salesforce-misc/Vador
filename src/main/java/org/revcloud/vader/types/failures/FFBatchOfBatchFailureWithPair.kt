package org.revcloud.vader.types.failures

import io.vavr.Tuple2
import io.vavr.control.Either

/**
 * This is an inline wrapper to represent `FFBatchOfBatchFailure`
 */
@JvmInline
value class FFBatchOfBatchFailureWithPair<ContainerPairT, MemberPairT, FailureT>(val failure: Either<Tuple2<ContainerPairT?, FailureT?>?, List<Tuple2<MemberPairT?, FailureT?>>>) {
  val containerFailure: Tuple2<ContainerPairT?, FailureT?>?
    get() = if (failure.isLeft) failure.left else null

  val batchMemberFailures: List<Tuple2<MemberPairT?, FailureT?>>
    get() = if (failure.isRight) failure.get() else emptyList()
}
