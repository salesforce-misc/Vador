package org.revcloud.vader.runner;

import io.vavr.Tuple2;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.types.Validator;
import org.revcloud.vader.types.ValidatorEtr;

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
abstract class BaseContainerValidationConfig<ContainerValidatableT, FailureT> {
  @Nullable protected Tuple2<@NonNull Integer, @Nullable FailureT> shouldHaveMinBatchSizeOrFailWith;
  @Nullable protected Tuple2<@NonNull Integer, @Nullable FailureT> shouldHaveMaxBatchSizeOrFailWith;

  @Singular
  protected Collection<ValidatorEtr<ContainerValidatableT, FailureT>> withContainerValidatorEtrs;

  @Nullable
  protected Tuple2<
          @NonNull Collection<Validator<? super ContainerValidatableT, @Nullable FailureT>>,
          @Nullable FailureT>
      withContainerValidators;

  @Singular("withContainerValidator")
  protected Map<Validator<? super ContainerValidatableT, FailureT>, FailureT>
      withContainerValidator;

  public List<ValidatorEtr<ContainerValidatableT, @Nullable FailureT>> getContainerValidators() {
    return ContainerValidationConfigEx.getContainerValidatorsEx(this);
  }
}
