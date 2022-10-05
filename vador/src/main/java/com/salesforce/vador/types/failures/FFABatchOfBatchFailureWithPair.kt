/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.types.failures

import io.vavr.Tuple2
import io.vavr.control.Either
import java.util.Optional

class FFABatchOfBatchFailureWithPair<ContainerPairT, MemberPairT, FailureT>(val failure: Either<Tuple2<ContainerPairT?, FailureT?>, Tuple2<MemberPairT?, FailureT?>>) :
  Either<Tuple2<ContainerPairT?, FailureT?>, Tuple2<MemberPairT?, FailureT?>> by failure {
  val containerFailure: Optional<Tuple2<ContainerPairT?, FailureT?>>
    get() = failure.swap().toJavaOptional()

  val batchMemberFailure: Optional<Tuple2<MemberPairT?, FailureT?>>
    get() = failure.toJavaOptional()

  val isContainerValid: Boolean
    get() = failure.isRight

  val isBatchMemberValid: Boolean
    get() = failure.isLeft
}
