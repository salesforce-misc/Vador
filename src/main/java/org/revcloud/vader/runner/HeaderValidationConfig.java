package org.revcloud.vader.runner;

import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Tuple2;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@Builder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class HeaderValidationConfig<HeaderValidatableT, FailureT> {
  @Singular Collection<TypedPropertyGetter<HeaderValidatableT, Collection<?>>> withBatchMappers;
  @Nullable Tuple2<@NonNull Integer, FailureT> shouldHaveMinBatchSize;
  @Nullable Tuple2<@NonNull Integer, FailureT> shouldHaveMaxBatchSize;
  @Singular Collection<Validator<HeaderValidatableT, FailureT>> withHeaderValidators;

  @Nullable
  Tuple2<@NonNull Collection<SimpleValidator<? super HeaderValidatableT, FailureT>>, FailureT>
      withSimpleHeaderValidators;

  @Singular("withSimpleHeaderValidator")
  Map<@NonNull SimpleValidator<? super HeaderValidatableT, FailureT>, FailureT>
      withSimpleHeaderValidator;

  List<Validator<HeaderValidatableT, FailureT>> getHeaderValidators() {
    return HeaderValidationConfigEx.getHeaderValidatorsEx(this);
  }

  public Set<String> getFieldNamesForBatch(Class<HeaderValidatableT> validatableClazz) {
    return HeaderValidationConfigEx.getFieldNamesForBatchEx(this, validatableClazz);
  }
}
