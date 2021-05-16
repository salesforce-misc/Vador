/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.failure;

import lombok.Data;

/**
 * Reference Validation Failure
 *
 * @author gakshintala
 * @since 228
 */
@Data
public class ValidationFailure {
  public static final ValidationFailure NONE = new ValidationFailure(ValidationFailureMessage.NONE);
  public static final ValidationFailure NOTHING_TO_VALIDATE =
      new ValidationFailure(ValidationFailureMessage.NOTHING_TO_VALIDATE);
  public static final ValidationFailure DUPLICATE_ITEM =
      new ValidationFailure(ValidationFailureMessage.DUPLICATE_ITEM);
  public static final ValidationFailure NULL_KEY =
      new ValidationFailure(ValidationFailureMessage.NULL_KEY);
  public static final ValidationFailure INVALID_CONTAINER =
      new ValidationFailure(ValidationFailureMessage.INVALID_PARENT);
  public static final ValidationFailure INVALID_MEMBER =
      new ValidationFailure(ValidationFailureMessage.INVALID_CHILD);
  public static final ValidationFailure UNKNOWN_EXCEPTION =
      new ValidationFailure(ValidationFailureMessage.UNKNOWN_EXCEPTION);
  public static final ValidationFailure VALIDATION_FAILURE_1 =
      new ValidationFailure(ValidationFailureMessage.VALIDATION_FAILURE_1);
  public static final ValidationFailure VALIDATION_FAILURE_2 =
      new ValidationFailure(ValidationFailureMessage.VALIDATION_FAILURE_2);
  public static final ValidationFailure VALIDATION_FAILURE_3 =
      new ValidationFailure(ValidationFailureMessage.VALIDATION_FAILURE_3);
  public static final ValidationFailure REQUIRED_FIELD_MISSING =
      new ValidationFailure(ValidationFailureMessage.REQUIRED_FIELD_MISSING);
  public static final ValidationFailure INVALID_COMBO_1 =
      new ValidationFailure(ValidationFailureMessage.INVALID_COMBO_1);
  public static final ValidationFailure INVALID_COMBO_2 =
      new ValidationFailure(ValidationFailureMessage.INVALID_COMBO_2);
  public static final ValidationFailure INVALID_VALUE =
      new ValidationFailure(ValidationFailureMessage.INVALID_VALUE);
  public static final ValidationFailure FIELD_INTEGRITY_EXCEPTION =
      new ValidationFailure(ValidationFailureMessage.FIELD_INTEGRITY_EXCEPTION);
  public static final ValidationFailure MIN_BATCH_SIZE_NOT_MET =
      new ValidationFailure(ValidationFailureMessage.MIN_BATCH_SIZE_EXCEEDED);
  public static final ValidationFailure MAX_BATCH_SIZE_EXCEEDED =
      new ValidationFailure(ValidationFailureMessage.MAX_BATCH_SIZE_EXCEEDED);
  private final ValidationFailureMessage validationFailureMessage;
  private String exceptionMsg;

  /**
   * Static factory method used to generate Validation failure out of an Exception.
   *
   * @param e The exception
   * @return Respective Validation failure for the exception.
   */
  public static ValidationFailure getValidationFailureForException(Throwable e) {
    final var unknownException = ValidationFailureMessage.UNKNOWN_EXCEPTION;
    final var validationFailure = new ValidationFailure(unknownException);
    validationFailure.setExceptionMsg(e.getMessage());
    return validationFailure;
  }

  public static ValidationFailure getFailureWithParams(
      ValidationFailureMessage validationFailureMessage, Object... params) {
    validationFailureMessage.setParams(params);
    return new ValidationFailure(validationFailureMessage);
  }
}
