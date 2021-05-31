package org.revcloud.vader.runner;

import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Tuple2;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.types.validators.Validator;
import org.revcloud.vader.types.validators.ValidatorEtr;

@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@Builder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class HeaderValidationConfig<HeaderValidatableT, FailureT> {
  @Singular Collection<TypedPropertyGetter<HeaderValidatableT, Collection<?>>> withBatchMappers;
  @Nullable Tuple2<@NonNull Integer, FailureT> shouldHaveMinBatchSize;
  @Nullable Tuple2<@NonNull Integer, FailureT> shouldHaveMaxBatchSize;
  @Singular Collection<ValidatorEtr<HeaderValidatableT, FailureT>> withHeaderValidatorEtrs;

  @Nullable
  Tuple2<@NonNull Collection<Validator<? super HeaderValidatableT, FailureT>>, FailureT>
      withHeaderValidators;

  @Singular("withHeaderValidator")
  Map<Validator<? super HeaderValidatableT, FailureT>, FailureT> withHeaderValidator;

  List<ValidatorEtr<HeaderValidatableT, FailureT>> getHeaderValidators() {
    return HeaderValidationConfigEx.getHeaderValidatorsEx(this);
  }

  public Set<String> getFieldNamesForBatch(Class<HeaderValidatableT> validatableClazz) {
    return HeaderValidationConfigEx.getFieldNamesForBatchEx(this, validatableClazz);
  }
}
