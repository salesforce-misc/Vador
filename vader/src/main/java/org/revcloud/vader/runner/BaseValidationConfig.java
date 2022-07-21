package org.revcloud.vader.runner;

import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Function2;
import io.vavr.Tuple2;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.runner.FieldConfig.FieldConfigBuilder;
import org.revcloud.vader.runner.IDConfig.IDConfigBuilder;
import org.revcloud.vader.specs.Spec;
import org.revcloud.vader.specs.Specs;
import org.revcloud.vader.specs.specs.BaseSpec;
import org.revcloud.vader.types.Validator;
import org.revcloud.vader.types.ValidatorEtr;

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
abstract class BaseValidationConfig<ValidatableT, FailureT> {

  @Singular("shouldHaveFieldOrFailWith")
  protected Map<TypedPropertyGetter<ValidatableT, ?>, FailureT> shouldHaveFieldsOrFailWith;

  @Nullable
  protected Tuple2<
          @NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, ?>>,
          @NonNull Function2<String, Object, @Nullable FailureT>>
      shouldHaveFieldsOrFailWithFn;

  @Singular("shouldHaveFieldOrFailWithFn")
  protected Map<TypedPropertyGetter<ValidatableT, ?>, Function2<String, Object, FailureT>>
      shouldHaveFieldOrFailWithFn;

  /** --- ID ---> */
  @Singular @Nullable
  protected Collection<IDConfigBuilder<?, ValidatableT, FailureT, ?>> withIdConfigs;

  @Singular @Nullable
  protected Collection<FieldConfigBuilder<?, ValidatableT, FailureT>> withFieldConfigs;

  @Nullable protected Specs<ValidatableT, FailureT> specify;

  @Singular("withSpec")
  protected Collection<Spec<ValidatableT, FailureT>> withSpecs;

  @Singular Collection<ValidatorEtr<ValidatableT, FailureT>> withValidatorEtrs;

  @Nullable
  Tuple2<
          @NonNull Collection<? extends Validator<? super ValidatableT, @Nullable FailureT>>,
          @NonNull FailureT>
      withValidators;

  /**
   * spotless:off
   * `withValidators` is used for the above combination. 
   * `withValidator` is meant to be used when passing individual parameters like:
   * ValidationConfig.<Bean, ValidationFailure>toValidate()
   *             .withValidator(validator1, failure1)
   *             .withValidator(validator2, failure2)
   * spotless:on
   */
  @Singular("withValidator")
  Map<? extends Validator<? super ValidatableT, FailureT>, FailureT> withValidator;

  // ! TODO 05/08/21 gopala.akshintala: Migrate them to be used with custom assertions
  List<BaseSpec<ValidatableT, FailureT>> getSpecs() {
    return BaseValidationConfigEx.getSpecsEx(this);
  }

  public Optional<Predicate<ValidatableT>> getPredicateOfSpecForTest(@NonNull String nameForTest) {
    return BaseValidationConfigEx.getPredicateOfSpecForTestEx(this, nameForTest);
  }

  public Set<String> getRequiredFieldNames(Class<ValidatableT> beanClass) {
    return BaseValidationConfigEx.getRequiredFieldNamesEx(this, beanClass);
  }
}
