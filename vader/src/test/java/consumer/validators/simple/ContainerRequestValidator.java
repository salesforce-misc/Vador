/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.validators.simple;

import static consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

import consumer.bean.Container;
import consumer.failure.ValidationFailure;
import org.revcloud.vader.types.validators.Validator;

public class ContainerRequestValidator {

  /**
   * Validates if Auth id in request has a status PROCESSED. This is a lambda function
   * implementation.
   */
  public static final Validator<Container, ValidationFailure> validation1 =
      container -> {
        if (container.getMember() == null) {
          return null;
        } else {
          return new ValidationFailure(FIELD_NULL_OR_EMPTY);
        }
      };

  public static final Validator<Container, ValidationFailure> validation2 =
      container -> {
        if (container.getMember() == null) {
          return null;
        } else {
          return new ValidationFailure(FIELD_NULL_OR_EMPTY);
        }
      };
}
