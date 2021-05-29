/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.validators.batch;

import static consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

import consumer.bean.Member;
import consumer.failure.ValidationFailure;
import io.vavr.control.Either;
import java.util.Objects;
import org.revcloud.vader.types.validators.ValidatorEtr;

public class MemberBatchRequestValidator {

  /**
   * Validates if Auth id in request has a status PROCESSED. This is a lambda function
   * implementation.
   */
  public static final ValidatorEtr<Member, ValidationFailure> batchValidation1 =
      member ->
          member
              .filter(Objects::isNull)
              .getOrElse(Either.left(new ValidationFailure(FIELD_NULL_OR_EMPTY)));

  public static final ValidatorEtr<Member, ValidationFailure> batchValidation2 =
      member ->
          member.filterOrElse(
              Objects::isNull, ignore -> new ValidationFailure(FIELD_NULL_OR_EMPTY));
}
