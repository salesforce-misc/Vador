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
import org.revcloud.vader.types.validators.Validator;
import org.revcloud.vader.types.validators.ValidatorEtr;

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
abstract class BaseHeaderValidationConfig<HeaderValidatableT, FailureT> {
  @Nullable protected Tuple2<@NonNull Integer, FailureT> shouldHaveMinBatchSize;
  @Nullable protected Tuple2<@NonNull Integer, FailureT> shouldHaveMaxBatchSize;
  @Singular protected Collection<ValidatorEtr<HeaderValidatableT, FailureT>> withHeaderValidatorEtrs;

  @Nullable
  protected Tuple2<@NonNull Collection<Validator<? super HeaderValidatableT, FailureT>>, FailureT>
      withHeaderValidators;

  @Singular("withHeaderValidator")
  protected Map<Validator<? super HeaderValidatableT, FailureT>, FailureT> withHeaderValidator;

  List<ValidatorEtr<HeaderValidatableT, FailureT>> getHeaderValidators() {
    return HeaderValidationConfigEx.getHeaderValidatorsEx(this);
  }
}
