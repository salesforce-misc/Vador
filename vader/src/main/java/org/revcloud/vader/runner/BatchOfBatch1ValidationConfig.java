/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.runner;

import io.vavr.Function1;
import io.vavr.Tuple2;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;

/**
 * This should be used for Batch that contains (HAS-A) a nested Batch (of type `Collection`) member
 * and the member needs a BatchValidationConfig of its own.
 *
 * <p>* `1` in the data type indicates the number of batch member types this config supports.
 *
 * <p>* For other Simple fields, please use `liftUtil` to lift corresponding validators.
 *
 * @param <ContainerValidatableT> Container data type
 * @param <MemberValidatableT> Batch Member data type
 * @param <FailureT>
 */
@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class BatchOfBatch1ValidationConfig<ContainerValidatableT, MemberValidatableT, FailureT>
    extends BaseBatchValidationConfig<ContainerValidatableT, FailureT> {
  Tuple2<
          Function1<ContainerValidatableT, Collection<MemberValidatableT>>,
          BatchValidationConfig<MemberValidatableT, @Nullable FailureT>>
      withMemberBatchValidationConfig;
}
