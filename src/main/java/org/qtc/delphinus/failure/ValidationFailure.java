/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus.failure;


import org.qtc.delphinus.FailureMessageDetails;

import static org.qtc.delphinus.Dsl.NO_FAILURE_DETAILS;

public class ValidationFailure implements Failure {
    private final FailureMessageDetails failureMessageDetails;

    /**
     * Private constructor only to be used internally to instantiate SUCCESS and UNKNOWN_EXCEPTION cases.
     *
     */
    private ValidationFailure(String section, String name) {
        this.failureMessageDetails = new FailureMessageDetails(section, name);
    }
    
    public FailureMessageDetails getFailureMessageDetails() {
        return failureMessageDetails;
    }

    @Override
    public String getExceptionMessageFromFailure() {
        return "";
    }
    
}
