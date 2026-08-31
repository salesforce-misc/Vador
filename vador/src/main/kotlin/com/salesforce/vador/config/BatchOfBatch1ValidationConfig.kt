/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.config

import com.salesforce.vador.config.base.BaseBatchValidationConfig
import com.salesforce.vador.immutables.ConfigStyle
import io.vavr.Function1
import io.vavr.Tuple2
import org.immutables.value.Value

/**
 * This should be used for Batch that contains (HAS-A) a nested Batch (of type `Collection`) member
 * and the member needs a BatchValidationConfig of its own.
 *
 * `1` in the data type indicates the number of batch member types this config supports.
 *
 * For other Simple fields, please use `liftUtil` to lift corresponding validators.
 *
 * @param <ContainerValidatableT> Container data type
 * @param <MemberValidatableT> Batch Member data type
 * @param <FailureT> Failure data type
 */
@ConfigStyle
@Value.Immutable(copy = false)
internal abstract class AbstractBatchOfBatch1ValidationConfig<
  ContainerValidatableT,
  MemberValidatableT,
  FailureT,
> : AbstractBatchValidationConfigSupport<ContainerValidatableT, FailureT>() {

  abstract val withMemberBatchValidationConfig:
    Tuple2<
      Function1<ContainerValidatableT, Collection<MemberValidatableT>>,
      out BaseBatchValidationConfig<MemberValidatableT, FailureT?>,
    >

  abstract class Builder<ContainerValidatableT, MemberValidatableT, FailureT> :
    ValidationBuilderDsl<
      ContainerValidatableT,
      FailureT,
      BatchOfBatch1ValidationConfig.Builder<ContainerValidatableT, MemberValidatableT, FailureT>,
    >,
    BatchValidationBuilderDsl<
      ContainerValidatableT,
      FailureT,
      BatchOfBatch1ValidationConfig.Builder<ContainerValidatableT, MemberValidatableT, FailureT>,
    >
}
