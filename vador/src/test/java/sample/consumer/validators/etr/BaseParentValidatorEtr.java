/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package sample.consumer.validators.etr;

import static sample.consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

import org.revcloud.vador.types.ValidatorEtr;
import sample.consumer.bean.Parent;
import sample.consumer.failure.ValidationFailure;

public class BaseParentValidatorEtr {

  /**
   * Validates if Auth id in request has a status PROCESSED. This is a lambda function
   * implementation.
   */
  public static final ValidatorEtr<Parent, ValidationFailure> validatorEtr1 =
      parentInputRepresentation ->
          parentInputRepresentation.filterOrElse(
              parent -> parent.getMember() != null,
              ignore -> new ValidationFailure(FIELD_NULL_OR_EMPTY));

  public static final ValidatorEtr<Parent, ValidationFailure> validatorEtr2 =
      parentInputRepresentation ->
          parentInputRepresentation.filterOrElse(
              parent -> parent.getMember() != null,
              ignore -> new ValidationFailure(FIELD_NULL_OR_EMPTY));
}
