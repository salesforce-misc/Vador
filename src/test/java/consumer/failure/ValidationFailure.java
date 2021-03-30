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
    private final ValidationFailureMessage validationFailureMessage;
    private String[] params;
    private String exceptionMsg;

    public static final ValidationFailure NONE = new ValidationFailure(ValidationFailureMessage.SUCCESS);
    public static final ValidationFailure NOTHING_TO_VALIDATE = new ValidationFailure(ValidationFailureMessage.NOTHING_TO_VALIDATE);
    public static final ValidationFailure DUPLICATE_ITEM = new ValidationFailure(ValidationFailureMessage.DUPLICATE_ITEM);
    public static final ValidationFailure INVALID_PARENT = new ValidationFailure(ValidationFailureMessage.INVALID_PARENT);
    public static final ValidationFailure INVALID_CHILD = new ValidationFailure(ValidationFailureMessage.INVALID_CHILD);
    public static final ValidationFailure UNKNOWN_EXCEPTION = new ValidationFailure(ValidationFailureMessage.UNKNOWN_EXCEPTION);
    public static final ValidationFailure VALIDATION_FAILURE_1 = new ValidationFailure(ValidationFailureMessage.VALIDATION_FAILURE_1);
    public static final ValidationFailure VALIDATION_FAILURE_2 = new ValidationFailure(ValidationFailureMessage.VALIDATION_FAILURE_2);
    public static final ValidationFailure VALIDATION_FAILURE_3 = new ValidationFailure(ValidationFailureMessage.VALIDATION_FAILURE_3);
    public static final ValidationFailure REQUIRED_FIELD_MISSING = new ValidationFailure(ValidationFailureMessage.REQUIRED_FIELD_MISSING);
    public static final ValidationFailure FIELD_INTEGRITY_EXCEPTION = new ValidationFailure(ValidationFailureMessage.FIELD_INTEGRITY_EXCEPTION);

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
}
