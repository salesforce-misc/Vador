package org.revcloud.vader.types.failures

import io.vavr.Tuple2
import io.vavr.control.Either

/**
 * This is an wrapper to represent `FFEBatchOfBatchFailure`
 */
class FFEBatchOfBatchFailureWithPair<ContainerPairT, MemberPairT, FailureT>(val failure: Either<Tuple2<ContainerPairT?, FailureT?>?, List<Tuple2<MemberPairT?, FailureT?>>>) : Either<Tuple2<ContainerPairT?, FailureT?>?, List<Tuple2<MemberPairT?, FailureT?>>> by failure {
  val containerFailure: Tuple2<ContainerPairT?, FailureT?>?
    get() = if (failure.isLeft) failure.left else null

  val batchMemberFailures: List<Tuple2<MemberPairT?, FailureT?>>
    get() = if (failure.isRight) failure.get() else emptyList()
}
