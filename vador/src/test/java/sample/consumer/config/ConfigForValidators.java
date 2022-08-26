/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package sample.consumer.config;

import io.vavr.collection.List;
import lombok.experimental.UtilityClass;
import org.revcloud.vador.types.Validator;
import org.revcloud.vador.types.ValidatorEtr;
import sample.consumer.bean.Container;
import sample.consumer.bean.Parent;
import sample.consumer.failure.ValidationFailure;
import sample.consumer.validators.etr.BaseParentValidatorEtr;
import sample.consumer.validators.simple.BaseParentValidator;
import sample.consumer.validators.simple.ContainerValidator;

/** gakshintala created on 4/13/20. */
@UtilityClass
public class ConfigForValidators {

  public static List<ValidatorEtr<Parent, ValidationFailure>> getServiceValidations() {
    return List.of(BaseParentValidatorEtr.validatorEtr1, BaseParentValidatorEtr.validatorEtr2);
  }

  public static List<ValidatorEtr<Container, ValidationFailure>> getParentValidations() {
    return null;
  }

  public static List<Validator<Container, ValidationFailure>> getParentSimpleValidations() {
    return List.of(ContainerValidator.validator1, ContainerValidator.validator2);
  }

  public static List<Validator<? extends Parent, ValidationFailure>> getSimpleServiceValidations() {
    return List.of(BaseParentValidator.validator1, ContainerValidator.validator1);
  }
}
