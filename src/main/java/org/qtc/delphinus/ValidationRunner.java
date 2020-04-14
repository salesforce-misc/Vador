/*
 * Copyright 2018 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus;

import com.google.common.collect.ImmutableList;

import java.util.function.Function;


/**
 * Singleton class to run all validations on the validatable
 * and returns the first failed validation.
 */
final class ValidationRunner {

    private ValidationRunner() {
    }

    /**
     * Method to run all request validations on Validatable in fail-fast mode and returns the first validation failure.
     *
     * @param request            Generic Input representation to validate.
     * @param requestValidations Validations to be performed on the request.
     * @param <RequestT>         Input Representation type to be validated
     * @return Validation failure if any, ValidationFailure.SUCCESS otherwise.
     */
    public static <FailureT, RequestT> FailureT validateFailFast(RequestT request,
                                                                ImmutableList<RequestValidation<FailureT, RequestT>> requestValidations, 
                                                                 FailureT invalidRequest) {
        if (request == null || requestValidations == null) {
            return invalidRequest;
        }
        return requestValidations.stream()
                .map(handleThrowableRequestValidation(requestValidation ->
                                                              requestValidation.validate(request)))
                .filter(validationFailure -> validationFailure != null) //todo
                .findFirst()
                .orElse(invalidRequest); // TODO: 4/13/20 fix 
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
