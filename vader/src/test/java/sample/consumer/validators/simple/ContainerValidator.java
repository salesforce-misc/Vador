/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package sample.consumer.validators.simple;

import static sample.consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

import org.revcloud.vader.types.validators.Validator;
import sample.consumer.bean.Container;
import sample.consumer.failure.ValidationFailure;

public class ContainerValidator {

  public static final Validator<Container, ValidationFailure> validator1 =
      container -> {
        if (container.getMember() == null) {
          return new ValidationFailure(FIELD_NULL_OR_EMPTY);
        } else {
          return ValidationFailure.NONE;
        }
      };

  public static final Validator<Container, ValidationFailure> validator2 =
      container -> {
        if (container.getMember() == null) {
          return new ValidationFailure(FIELD_NULL_OR_EMPTY);
        } else {
          return ValidationFailure.NONE;
        }
      };
}
