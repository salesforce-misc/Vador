/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus;


import java.util.Arrays;
import java.util.Objects;

import static org.qtc.delphinus.Dsl.GenericValidationFailureMessage.UNKNOWN_EXCEPTION;


/**
 * This class is the unit of exchange for all validation functions.
 *
 * @author gakshintala
 * @since 220
 */
public class ValidationFailure {
    public static final ValidationFailure NONE = new ValidationFailure(Dsl.GenericValidationFailureMessage.NONE);
    private final ValidationFailureMessage validationFailureMessage;
    private String[] params;

    public ValidationFailure(
            ValidationFailureMessage validationFailureMessage, String... params) {
        Objects.requireNonNull("apiErrorCode for ValidationFailure cannot be null");
        Objects.requireNonNull(validationFailureMessage,
                               "validationFailureMessage for ValidationFailure cannot be null");
        Objects.requireNonNull(params, "params for ValidationFailure cannot be null");
        this.validationFailureMessage = validationFailureMessage;
        this.params = params;
    }

    /**
     * Private constructor only to be used internally to instantiate SUCCESS and UNKNOWN_EXCEPTION cases.
     *
     * @param validationFailureMessage validation failure message.
     */
    private ValidationFailure(ValidationFailureMessage validationFailureMessage) {
        this.validationFailureMessage = validationFailureMessage;
    }

    /**
     * Static factory method used to generate Validation failure out of an Exception.
     *
     * @param e The exception
     * @return Respective Validation failure for the exception.
     */
    public static ValidationFailure getValidationFailureForException(Throwable e) {
        return new ValidationFailure(UNKNOWN_EXCEPTION) {
            
        };
    }
    
    public ValidationFailureMessage getValidationFailureMessage() {
        return validationFailureMessage;
    }

    public String[] getParams() {
        return params != null ? Arrays.stream(params).toArray(String[]::new) : null; // For immutability.
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValidationFailure)) return false;

        ValidationFailure that = (ValidationFailure) o;

        if (!getValidationFailureMessage().equals(that.getValidationFailureMessage())) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(getParams(), that.getParams());
    }

    @Override
    public int hashCode() {
        int result = getValidationFailureMessage().hashCode();
        result = 31 * result + Arrays.hashCode(getParams());
        return result;
    }

    @Override
    public String toString() {
        return "ValidationFailure{" +
               "validationFailureMessage=" + validationFailureMessage +
               ", params=" + Arrays.toString(params) +
               '}';
    }
}
