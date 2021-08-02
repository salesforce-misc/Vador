package org.revcloud.vader.runner;

import com.force.swag.id.ID;
import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Function2;
import io.vavr.Tuple2;
import java.util.*;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.runner.SpecFactory.BaseSpec;
import org.revcloud.vader.types.validators.Validator;
import org.revcloud.vader.types.validators.ValidatorEtr;

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
abstract class BaseValidationConfig<ValidatableT, FailureT> {

  @Singular("shouldHaveFieldOrFailWith")
  protected Map<TypedPropertyGetter<ValidatableT, ?>, @Nullable FailureT>
      shouldHaveFieldsOrFailWith;

  @Nullable
  protected Tuple2<
          @NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, ?>>,
          @NonNull Function2<String, Object, @Nullable FailureT>>
      shouldHaveFieldsOrFailWithFn;

  @Singular("shouldHaveFieldOrFailWithFn")
  protected Map<
          TypedPropertyGetter<ValidatableT, ?>,
          @NonNull Function2<String, Object, @Nullable FailureT>>
      shouldHaveFieldOrFailWithFn;

  @Singular("shouldHaveValidSFIdFormatOrFailWith")
  protected Map<TypedPropertyGetter<ValidatableT, ID>, @Nullable FailureT>
      shouldHaveValidSFIdFormatForAllOrFailWith;

  @Nullable
  protected Tuple2<
          @NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, ID>>,
          @NonNull Function2<String, ID, @Nullable FailureT>>
      shouldHaveValidSFIdFormatForAllOrFailWithFn;

  @Singular("shouldHaveValidSFIdFormatOrFailWithFn")
  protected Map<
          TypedPropertyGetter<ValidatableT, ID>, @NonNull Function2<String, ID, @Nullable FailureT>>
      shouldHaveValidSFIdFormatOrFailWithFn;

  @Singular("absentOrHaveValidSFIdFormatOrFailWith")
  protected Map<TypedPropertyGetter<ValidatableT, ID>, @Nullable FailureT>
      absentOrHaveValidSFIdFormatForAllOrFailWith;

  @Nullable
  protected Tuple2<
          @NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, ID>>,
          @NonNull Function2<String, ID, @Nullable FailureT>>
      absentOrHaveValidSFIdFormatForAllOrFailWithFn;

  @Singular("absentOrHaveValidSFIdFormatOrFailWithFn")
  protected Map<
          TypedPropertyGetter<ValidatableT, ID>, @NonNull Function2<String, ID, @Nullable FailureT>>
      absentOrHaveValidSFIdFormatOrFailWithFn;

  @Nullable protected Specs<ValidatableT, FailureT> specify;

  @Singular("withSpec")
  protected Collection<? extends Spec<ValidatableT, @Nullable FailureT>> withSpecs;

  @Singular Collection<ValidatorEtr<ValidatableT, @Nullable FailureT>> withValidatorEtrs;

  @Nullable
  Tuple2<
          @NonNull Collection<? extends Validator<? super ValidatableT, @Nullable FailureT>>,
          @NonNull FailureT>
      withValidators;

  @Singular("withValidator")
  Map<? extends Validator<? super ValidatableT, FailureT>, @Nullable FailureT> withValidator;

  List<BaseSpec<ValidatableT, FailureT>> getSpecs() {
    return BaseValidationConfigEx.getSpecsEx(this);
  }

  public Optional<Predicate<ValidatableT>> getPredicateOfSpecForTest(@NonNull String nameForTest) {
    return BaseValidationConfigEx.getPredicateOfSpecForTestEx(this, nameForTest);
  }

  public Set<String> getRequiredFieldNames(Class<ValidatableT> beanClass) {
    return BaseValidationConfigEx.getRequiredFieldNamesEx(this, beanClass);
  }

  public Set<String> getRequiredFieldNamesForSFIdFormat(Class<ValidatableT> beanClass) {
    return BaseValidationConfigEx.getRequiredFieldNamesForSFIdFormatEx(this, beanClass);
  }

  public Set<String> getNonRequiredFieldNamesForSFIdFormat(Class<ValidatableT> beanClass) {
    return BaseValidationConfigEx.getNonRequiredFieldNamesForSFIdFormatEx(this, beanClass);
  }
}
