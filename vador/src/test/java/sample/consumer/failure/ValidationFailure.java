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

package sample.consumer.failure;

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
  public static final ValidationFailure DUPLICATE_ITEM_1 =
      new ValidationFailure(ValidationFailureMessage.DUPLICATE_ITEM_1);
  public static final ValidationFailure DUPLICATE_ITEM_2 =
      new ValidationFailure(ValidationFailureMessage.DUPLICATE_ITEM_2);
  public static final ValidationFailure NULL_KEY =
      new ValidationFailure(ValidationFailureMessage.NULL_KEY);
  public static final ValidationFailure INVALID_BEAN =
      new ValidationFailure(ValidationFailureMessage.INVALID_BEAN);
  public static final ValidationFailure INVALID_BEAN_1 =
      new ValidationFailure(ValidationFailureMessage.INVALID_BEAN_1);
  public static final ValidationFailure INVALID_BEAN_2 =
      new ValidationFailure(ValidationFailureMessage.INVALID_BEAN_2);
  public static final ValidationFailure INVALID_CONTAINER =
      new ValidationFailure(ValidationFailureMessage.INVALID_PARENT);
  public static final ValidationFailure INVALID_ITEM =
      new ValidationFailure(ValidationFailureMessage.INVALID_ITEM);
  public static final ValidationFailure INVALID_UDD_ID =
      new ValidationFailure(ValidationFailureMessage.INVALID_UDD_ID);
  public static final ValidationFailure INVALID_POLYMORPHIC_UDD_ID =
      new ValidationFailure(ValidationFailureMessage.INVALID_POLYMORPHIC_UDD_ID);
  public static final ValidationFailure INVALID_OPTIONAL_UDD_ID =
      new ValidationFailure(ValidationFailureMessage.INVALID_OPTIONAL_UDD_ID);
  public static final ValidationFailure INVALID_UDD_ID_2 =
      new ValidationFailure(ValidationFailureMessage.INVALID_UDD_ID_2);
  public static final ValidationFailure INVALID_UDD_ID_3 =
      new ValidationFailure(ValidationFailureMessage.INVALID_UDD_ID_3);
  public static final ValidationFailure INVALID_MEMBER =
      new ValidationFailure(ValidationFailureMessage.INVALID_CHILD);
  public static final ValidationFailure OUT_OF_BOUND =
      new ValidationFailure(ValidationFailureMessage.UNKNOWN_EXCEPTION);
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
  public static final ValidationFailure REQUIRED_FIELD_MISSING_1 =
      new ValidationFailure(ValidationFailureMessage.REQUIRED_FIELD_MISSING_1);
  public static final ValidationFailure REQUIRED_FIELD_MISSING_2 =
      new ValidationFailure(ValidationFailureMessage.REQUIRED_FIELD_MISSING_2);
  public static final ValidationFailure REQUIRED_LIST_MISSING =
      new ValidationFailure(ValidationFailureMessage.REQUIRED_LIST_MISSING);
  public static final ValidationFailure INVALID_COMBO_1 =
      new ValidationFailure(ValidationFailureMessage.INVALID_COMBO_1);
  public static final ValidationFailure INVALID_COMBO_2 =
      new ValidationFailure(ValidationFailureMessage.INVALID_COMBO_2);
  public static final ValidationFailure INVALID_VALUE =
      new ValidationFailure(ValidationFailureMessage.INVALID_VALUE);
  public static final ValidationFailure FIELD_INTEGRITY_EXCEPTION =
      new ValidationFailure(ValidationFailureMessage.FIELD_INTEGRITY_EXCEPTION);
  public static final ValidationFailure MIN_BATCH_SIZE_NOT_MET =
      new ValidationFailure(ValidationFailureMessage.MIN_BATCH_SIZE_NOT_MET);
  public static final ValidationFailure MIN_BATCH_SIZE_NOT_MET_ROOT_LEVEL =
      new ValidationFailure(ValidationFailureMessage.MIN_BATCH_SIZE_NOT_MET_ROOT_LEVEL);
  public static final ValidationFailure MIN_BATCH_SIZE_NOT_MET_LEVEL_1 =
      new ValidationFailure(ValidationFailureMessage.MIN_BATCH_SIZE_NOT_MET_LEVEL_1);
  public static final ValidationFailure MAX_NESTED_BATCH_SIZE_EXCEEDED_2 =
      new ValidationFailure(ValidationFailureMessage.MAX_NESTED_BATCH_SIZE_EXCEEDED_2);
  public static final ValidationFailure MAX_BATCH_SIZE_EXCEEDED_LEVEL_2 =
      new ValidationFailure(ValidationFailureMessage.MAX_BATCH_SIZE_EXCEEDED_LEVEL_2);
  public static final ValidationFailure MAX_BATCH_SIZE_EXCEEDED_LEVEL_3 =
      new ValidationFailure(ValidationFailureMessage.MAX_BATCH_SIZE_EXCEEDED_LEVEL_3);
  public static final ValidationFailure MIN_BATCH_SIZE_NOT_MET_LEVEL_2 =
      new ValidationFailure(ValidationFailureMessage.MIN_BATCH_SIZE_NOT_MET_LEVEL_2);
  public static final ValidationFailure MAX_BATCH_SIZE_EXCEEDED =
      new ValidationFailure(ValidationFailureMessage.MAX_BATCH_SIZE_EXCEEDED);

  private final ValidationFailureMessage validationFailureMessage;
  private String exceptionMsg;

  /**
   * Static factory method used to generate Validation failures out of an Exception.
   *
   * @param e The exception
   * @return Respective Validation failures for the exception.
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

  public static ValidationFailure getFailureWithParams(
      ValidationFailure validationFailure, Object... params) {
    return getFailureWithParams(validationFailure.validationFailureMessage, params);
  }
}
