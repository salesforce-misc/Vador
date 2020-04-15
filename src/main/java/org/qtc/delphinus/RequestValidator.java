/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus;

import com.google.common.collect.ImmutableList;



/**
 * This needs to be instantiated with list of Validations in the sequence of their execution.
 *
 * @param <RequestT> Request to be validated.
 * @author gakshintala
 * @since 220
 */
public class RequestValidator<FailureT, RequestT> {

    private ImmutableList<RequestValidation<FailureT, RequestT>> requestValidations;

    public RequestValidator(ImmutableList<RequestValidation<FailureT, RequestT>> requestValidations) {
        this.requestValidations = requestValidations;
    }

    /**
     * Setter for request validations of a service.
     *
     * @param requestValidations request validations to set.
     */
    public void setRequestValidations(ImmutableList<RequestValidation<FailureT, RequestT>> requestValidations) {
        this.requestValidations = requestValidations;
    }

    /**
     * Method to be called to trigger validations on the request.
     * This is called from AbstractActionService.
     * Concrete service implementations should not get a need it call this explicitly.
     *
     * @param request Object to be validated.
     * @return Validation failure, if any. ValidationFailure.SUCCESS otherwise.
     */
    public FailureT validate(RequestT request) {
        return ValidationStrategy.validateFailFast(request, requestValidations, null); // todo fix
    }

}
