package org.revcloud.vader.runner;

import com.force.swag.id.ID;
import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple2;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.revcloud.vader.runner.SpecFactory.BaseSpec;
import org.revcloud.vader.runner.SpecFactory.BaseSpec.BaseSpecBuilder;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
abstract class BaseValidationConfig<ValidatableT, FailureT> {
  @Singular("shouldHaveFieldOrFailWith")
  protected Map<TypedPropertyGetter<ValidatableT, ?>, FailureT> shouldHaveFieldsOrFailWith;

  @Nullable
  protected Tuple2<
          @NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, ?>>,
          @NonNull Function2<String, Object, FailureT>>
      shouldHaveFieldsOrFailWithFn;

  @Singular("shouldHaveValidSFIdFormatOrFailWith")
  protected Map<TypedPropertyGetter<ValidatableT, ID>, FailureT>
      shouldHaveValidSFIdFormatOrFailWith;

  @Nullable
  protected Tuple2<
          @NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, ID>>,
          @NonNull Function2<String, ID, FailureT>>
      shouldHaveValidSFIdFormatOrFailWithFn;

  @Singular("absentOrHaveValidSFIdFieldsOrFailWith")
  protected Map<TypedPropertyGetter<ValidatableT, ID>, FailureT>
      absentOrHaveValidSFIdFieldsOrFailWith;

  @Nullable
  protected Tuple2<
          @NonNull Collection<@NonNull TypedPropertyGetter<ValidatableT, ID>>,
          @NonNull Function2<String, ID, FailureT>>
      absentOrHaveValidSFIdFormatOrFailWithFn;

  @Nullable
  protected Function1<
          @NonNull SpecFactory<ValidatableT, FailureT>,
          @NonNull Collection<@NonNull ? extends BaseSpecBuilder<ValidatableT, FailureT, ?, ?>>>
      specify;

  @Singular("withSpec")
  protected Collection<
          Function1<
              @NonNull SpecFactory<ValidatableT, FailureT>,
              @NonNull ? extends BaseSpecBuilder<ValidatableT, FailureT, ?, ?>>>
      withSpecs;

  @Singular Collection<Validator<ValidatableT, FailureT>> withValidators;

  @Nullable
  Tuple2<
          @NonNull Collection<? extends SimpleValidator<? super ValidatableT, FailureT>>,
          @NonNull FailureT>
      withSimpleValidators;

  @Singular("withSimpleValidator")
  Collection<
          Tuple2<
              @NonNull ? extends SimpleValidator<? super ValidatableT, FailureT>,
              @NonNull FailureT>>
      withSimpleValidator;

  List<BaseSpec<ValidatableT, FailureT>> getSpecs() {
    val specFactory = new SpecFactory<ValidatableT, FailureT>();
    return Stream.concat(
            Stream.ofNullable(specify)
                .flatMap(specs -> specs.apply(specFactory).stream().map(BaseSpecBuilder::done)),
            Stream.ofNullable(withSpecs)
                .flatMap(specs -> specs.stream().map(spec -> spec.apply(specFactory).done())))
        .collect(Collectors.toList());
  }

  public Optional<Predicate<ValidatableT>> getSpecWithName(@NonNull String nameForTest) {
    return BaseValidationConfigEx.getSpecWithNameEx(this, nameForTest);
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
