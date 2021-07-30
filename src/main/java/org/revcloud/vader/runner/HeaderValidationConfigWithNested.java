package org.revcloud.vader.runner;

import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Tuple2;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.types.validators.Validator;
import org.revcloud.vader.types.validators.ValidatorEtr;

@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class HeaderValidationConfigWithNested<
        HeaderValidatableT, NestedHeaderValidatableT, FailureT>
    extends BaseHeaderValidationConfig<HeaderValidatableT, FailureT> {

  @Singular
  Collection<TypedPropertyGetter<HeaderValidatableT, Collection<NestedHeaderValidatableT>>>
      withBatchMappers;

  @Singular
  Collection<TypedPropertyGetter<NestedHeaderValidatableT, Collection<?>>> withNestedBatchMappers;

  @Nullable Tuple2<@NonNull Integer, FailureT> shouldHaveMinNestedBatchSize;
  @Nullable Tuple2<@NonNull Integer, FailureT> shouldHaveMaxNestedBatchSize;
  @Singular Collection<ValidatorEtr<HeaderValidatableT, FailureT>> withHeaderValidatorEtrs;

  @Nullable
  Tuple2<@NonNull Collection<Validator<? super HeaderValidatableT, FailureT>>, FailureT>
      withHeaderValidators;

  @Singular("withHeaderValidator")
  Map<Validator<? super HeaderValidatableT, FailureT>, FailureT> withHeaderValidator;

  public Set<String> getFieldNamesForNestedBatch(Class<NestedHeaderValidatableT> validatableClazz) {
    return HeaderValidationConfigEx.getFieldNamesForNestedBatchEx(this, validatableClazz);
  }

  public Set<String> getFieldNamesForBatch(Class<HeaderValidatableT> validatableClazz) {
    return HeaderValidationConfigEx.getFieldNamesForBatchEx(this, validatableClazz);
  }
}
