/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus;

import common.api.ApiErrorCodes;
import connect.rest.validation.Dsl.GenericValidationFailureMessage;
import system.context.LC;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

import static connect.rest.validation.Dsl.GenericValidationFailureMessage.UNKNOWN_EXCEPTION;


/**
 * This class is the unit of exchange for all validation functions.
 *
 * @author gakshintala
 * @since 220
 */
public class ValidationFailure {
    public static final connect.rest.validation.failure.ValidationFailure NONE = new connect.rest.validation.failure.ValidationFailure(GenericValidationFailureMessage.NONE);
    private final ValidationFailureMessage validationFailureMessage;
    private String[] params;

    public ValidationFailure(
            @Nonnull ApiErrorCodes apiErrorCode, @Nonnull ValidationFailureMessage validationFailureMessage) {
        Objects.requireNonNull(apiErrorCode, "apiErrorCode for ValidationFailure cannot be null");
        Objects.requireNonNull(validationFailureMessage,
                               "validationFailureMessage for ValidationFailure cannot be null");
        this.validationFailureMessage = validationFailureMessage;
    }

    public ValidationFailure(
            @Nonnull ApiErrorCodes apiErrorCode,
            @Nonnull ValidationFailureMessage validationFailureMessage, @Nonnull String... params) {
        Objects.requireNonNull(apiErrorCode, "apiErrorCode for ValidationFailure cannot be null");
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
    public static connect.rest.validation.failure.ValidationFailure getValidationFailureForException(Throwable e) {
        return new connect.rest.validation.failure.ValidationFailure(UNKNOWN_EXCEPTION) {
            @Override
            public String getLocalizedErrorMessage() {
                return e.getMessage();
            }
        };
    }
    
    public ValidationFailureMessage getValidationFailureMessage() {
        return validationFailureMessage;
    }

    public String[] getParams() {
        return params != null ? Arrays.stream(params).toArray(String[]::new) : null; // For immutability.
    }

    /**
     * Method to get validation failure message in localized format.
     *
     * @return validation failure message in localized format.
     */
    public String getLocalizedErrorMessage() {
        return LC.getLabel(validationFailureMessage.getSection(), validationFailureMessage.getName(),
                           (Object[]) params);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof connect.rest.validation.failure.ValidationFailure)) return false;

        connect.rest.validation.failure.ValidationFailure that = (connect.rest.validation.failure.ValidationFailure) o;

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
