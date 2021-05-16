/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.validators.simple;

import static consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

import consumer.bean.Member;
import consumer.failure.ValidationFailure;
import org.revcloud.vader.types.validators.SimpleValidator;

public class MemberRequestValidator {

  /**
   * Validates if Auth id in request has a status PROCESSED. This is a lambda function
   * implementation.
   */
  public static final SimpleValidator<Member, ValidationFailure> validation1 =
      member -> {
        if (member == null) {
          return null;
        } else {
          return new ValidationFailure(FIELD_NULL_OR_EMPTY);
        }
      };

  static final String ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID =
      "PaymentStandardFields.PaymentAuthorizationId.getName()";
  static final SimpleValidator<Member, ValidationFailure> validation2 =
      member -> {
        if (member == null) {
          return null;
        } else {
          return new ValidationFailure(FIELD_NULL_OR_EMPTY);
        }
      };
}
