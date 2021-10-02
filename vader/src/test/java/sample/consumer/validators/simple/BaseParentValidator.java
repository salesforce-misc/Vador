/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package sample.consumer.validators.simple;

import static sample.consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

import org.revcloud.vader.types.validators.Validator;
import sample.consumer.bean.Parent;
import sample.consumer.failure.ValidationFailure;

public class BaseParentValidator {

  public static final Validator<Parent, ValidationFailure> validator1 =
      parent -> {
        if (parent.getMember() == null) {
          return null;
        } else {
          return new ValidationFailure(FIELD_NULL_OR_EMPTY);
        }
      };

  public static final Validator<Parent, ValidationFailure> validator2 =
      parent -> {
        if (parent.getMember() == null) {
          return null;
        } else {
          return new ValidationFailure(FIELD_NULL_OR_EMPTY);
        }
      };
}
