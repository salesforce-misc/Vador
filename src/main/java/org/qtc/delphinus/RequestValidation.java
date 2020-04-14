/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus;

/**
 * Represents a request validation function that accepts service request bean and produces a validation result.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #validate(RequestT)}.
 *
 * @param <RequestT> the type of request to be validated.
 * @author gakshintala
 * @since 220
 */
@FunctionalInterface
public interface RequestValidation<FailureT, RequestT> {

    /**
     * Applies this validation to the given object.
     *
     * @param request Type of Object to be validated
     * @return the Validation failure, if any, otherwise ValidationFailure.SUCCESS.
     */
    FailureT validate(RequestT request);
}
