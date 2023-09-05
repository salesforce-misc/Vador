/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.config.base;

import com.salesforce.vador.config.FieldConfig.FieldConfigBuilder;
import com.salesforce.vador.config.IDConfig.IDConfigBuilder;
import com.salesforce.vador.specs.specs.base.BaseSpec;
import com.salesforce.vador.types.Spec;
import com.salesforce.vador.types.Specs;
import com.salesforce.vador.types.Validator;
import com.salesforce.vador.types.ValidatorEtr;
import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Tuple2;
import java.lang.reflect.Type;
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

@Getter
@SuperBuilder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public abstract class BaseValidationConfig<ValidatableT, FailureT> {

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

  @Nullable Tuple2<Map<String, @Nullable FailureT>, @Nullable FailureT> forAnnotations;

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

  @Nullable Function1<ValidatableT, List<ValidatableT>> withRecursiveMapper;

  // ! TODO 05/08/21 gopala.akshintala: Migrate them to be used with custom assertions
  public List<BaseSpec<ValidatableT, FailureT>> getSpecs() {
    return BaseValidationConfigEx.getSpecsEx(this);
  }

  public Optional<Predicate<ValidatableT>> getPredicateOfSpecForTest(@NonNull String nameForTest) {
    return BaseValidationConfigEx.getPredicateOfSpecForTestEx(this, nameForTest);
  }

  public Set<String> getRequiredFieldNames(Class<ValidatableT> beanClass) {
    return BaseValidationConfigEx.getRequiredFieldNamesEx(this, beanClass);
  }

  public Type getValidatableType() {
    return BaseValidationConfigEx.getValidatableType(this);
  }
}
