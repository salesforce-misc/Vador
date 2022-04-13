/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package sample.consumer.validators.etr;

import java.util.Objects;
import org.revcloud.vader.types.ValidatorEtr;
import sample.consumer.bean.Member;
import sample.consumer.failure.ValidationFailure;
import sample.consumer.failure.ValidationFailureMessage;

public class MemberValidatorEtr {

  public static final ValidatorEtr<Member, ValidationFailure> validatorEtr1 =
      member ->
          member.filterOrElse(
              Objects::nonNull,
              ignore -> new ValidationFailure(ValidationFailureMessage.FIELD_NULL_OR_EMPTY));

  public static final ValidatorEtr<Member, ValidationFailure> validatorEtr2 =
      member ->
          member.filterOrElse(
              Objects::nonNull,
              ignore -> new ValidationFailure(ValidationFailureMessage.FIELD_NULL_OR_EMPTY));
}
