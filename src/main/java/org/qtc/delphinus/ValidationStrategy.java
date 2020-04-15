/*
 * Copyright 2018 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus;

import com.google.common.collect.ImmutableList;
import io.vavr.control.Either;

import java.util.function.Function;

import static org.qtc.delphinus.Dsl.NONE;


/**
 * Singleton class to run all validations on the validatable
 * and returns the first failed validation.
 */
final class ValidationStrategy {

    private ValidationStrategy() {
    }

    /**
     * Method to run all validatable validations on Validatable in fail-fast mode and returns the first validation failure.
     *
     * @param validatable            Generic Input representation to validate.
     * @param requestValidations Validations to be performed on the validatable.
     * @param <ValidatableT>         Input Representation type to be validated
     * @return Validation failure if any, ValidationFailure.SUCCESS otherwise.
     */
    public static <FailureT, ValidatableT> Either<FailureT, ValidatableT> validateFailFast(ValidatableT validatable,
                                                                     ImmutableList<RequestValidation<FailureT, ValidatableT>> requestValidations,
                                                                     FailureT invalidRequest) {
        if (validatable == null || requestValidations == null) {
            return Either.left(invalidRequest);
        }
        return requestValidations.stream()
                .map(handleThrowableRequestValidation(requestValidation ->
                                                              requestValidation.validate(validatable)))
                .filter(validationFailure -> validationFailure != NONE) // TODO: 4/15/20 No need of this as we migrate to Either. 
                .findFirst()
                .map(Either::<FailureT, ValidatableT>left)
                .orElse(Either.right(validatable)); 
    }

    /**
     * Handle if any exception gets thrown while running a request validation.
     *
     * @param requestValidation a requestValidation that runs a validation.
     * @param <RequestT>        Type of input representation for request validation.
     * @return Validation failure returned from the validation.
     */
    static <FailureT, RequestT> Function<RequestT, FailureT> handleThrowableRequestValidation(
            ThrowableRequestValidation<FailureT, RequestT> requestValidation) {
        return inputRepresentation -> {
            try {
                return requestValidation.validate(inputRepresentation);
            } catch (Throwable e) {
                return null; // TODO: 4/13/20 change 
            }
        };
    }

    /**
     * Represents a function that runs as a Validation and might throw a
     *
     * @param <InputRepresentationT> Type of input representation for request validation.
     */
    @FunctionalInterface
    private interface ThrowableRequestValidation<FailureT, InputRepresentationT> {
        FailureT validate(InputRepresentationT requestValidation) throws Throwable;
    }
}
