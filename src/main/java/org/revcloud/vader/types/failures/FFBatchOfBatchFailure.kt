package org.revcloud.vader.types.failures

import io.vavr.control.Either

/**
 * This is an inline wrapper to represent `FFBatchOfBatchFailure`
 */
@JvmInline
value class FFBatchOfBatchFailure<FailureT>(val failure: Either<FailureT?, List<FailureT?>>)
