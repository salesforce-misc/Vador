/*
 * Copyright 2020 salesforce.com, inc. 
 * All Rights Reserved 
 * Company Confidential
 */

package consumer.failure;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class is the unit of exchange for all validation functions.
 *
 * @author gakshintala
 * @since 220
 */
public class ValidationFailure {
    private ApiErrorCodes apiErrorCode;
    private ValidationFailureMessage validationFailureMessage;
    private String[] params;

    public static final ValidationFailure SUCCESS = new ValidationFailure(ValidationFailureMessage.SUCCESS);

    public ValidationFailure(ApiErrorCodes apiErrorCode, ValidationFailureMessage validationFailureMessage) {
        Objects.requireNonNull(apiErrorCode, "apiErrorCode for ValidationFailure cannot be null");
        Objects.requireNonNull(validationFailureMessage, "validationFailureMessage for ValidationFailure cannot be null");
        this.apiErrorCode = apiErrorCode;
        this.validationFailureMessage = validationFailureMessage;
    }

    public ValidationFailure(ApiErrorCodes apiErrorCode, ValidationFailureMessage validationFailureMessage, String... params) {
        Objects.requireNonNull(apiErrorCode, "apiErrorCode for ValidationFailure cannot be null");
        Objects.requireNonNull(validationFailureMessage, "validationFailureMessage for ValidationFailure cannot be null");
        Objects.requireNonNull(params, "params for ValidationFailure cannot be null");
        this.apiErrorCode = apiErrorCode;
        this.validationFailureMessage = validationFailureMessage;
        this.params = params;
    }

    /**
     * Private constructor only to be used internally to instantiate SUCCESS and UNKNOWN_EXCEPTION cases.
     * @param validationFailureMessage validation failure message.
     */
    private ValidationFailure(ValidationFailureMessage validationFailureMessage) {
        this.validationFailureMessage = validationFailureMessage;
    }

    public ApiErrorCodes getApiErrorCode() {
        return apiErrorCode;
    }

    public ValidationFailureMessage getValidationFailureMessage() {
        return validationFailureMessage;
    }

    public String[] getParams() {
        return params != null ? Arrays.stream(params).toArray(String[]::new) : null; // For immutability.
    }

    /**
     * Method to get validation failure message in localized format.
     * @return validation failure message in localized format.
     */


    /**
     * Static factory method used to generate Validation failure out of an Exception.
     * 
     * @param e The exception
     * @return Respective Validation failure for the exception. 
     */
    public static ValidationFailure getValidationFailureForException(Throwable e) {
        return new ValidationFailure(ValidationFailureMessage.UNKNOWN_EXCEPTION) {
            @Override
            public ApiErrorCodes getApiErrorCode() {
                if (e instanceof PaymentsRuntimeException) {
                    return ((PaymentsRuntimeException) e).getCodes();
                }
                return ApiErrorCodes.UNKNOWN_EXCEPTION;
            }

        };
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValidationFailure that = (ValidationFailure) o;
        return Objects.equals(apiErrorCode, that.apiErrorCode) 
               && validationFailureMessage == that.validationFailureMessage 
               && Arrays.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(apiErrorCode, validationFailureMessage);
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }

    @Override
    public String toString() {
        return "ValidationFailure{" +
               "apiErrorCode=" + apiErrorCode +
               ", validationFailureMessage=" + validationFailureMessage +
               ", params=" + Arrays.toString(params) +
               '}';
    }
}
