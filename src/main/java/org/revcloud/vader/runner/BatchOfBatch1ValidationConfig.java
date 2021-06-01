package org.revcloud.vader.runner;

import io.vavr.Function1;
import io.vavr.Tuple2;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * This should be used for Batch that contains (HAS-A) a nested Batch (of type `Collection`) member
 * and the member needs a BatchValidationConfig of its own.
 *
 * <p>- `1` in the data type indicates the level of depth it supports for the batch member. - For
 * other Simple fields, please use `liftUtil` to lift corresponding validators.
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
          BatchValidationConfig<MemberValidatableT, FailureT>>
      withMemberBatchValidationConfig;
}
