/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.revcloud.vader.specs.failure;

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
  public static final ValidationFailure INVALID_VALUE =
      new ValidationFailure(ValidationFailureMessage.INVALID_VALUE);
  private final ValidationFailureMessage validationFailureMessage;
  private String exceptionMsg;
}
