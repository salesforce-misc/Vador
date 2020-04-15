/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus.failure;


import org.qtc.delphinus.Dsl;
import org.qtc.delphinus.FailureMessageDetails;

public class ExceptionFailure implements Failure {
    private final String failureMessage;

    /**
     * Private constructor only to be used internally to instantiate SUCCESS and UNKNOWN_EXCEPTION cases.
     *
     * @param failureMessageDetails validation failure message.
     */
    public ExceptionFailure(Throwable throwable) {
        this.failureMessage = throwable.getMessage();
    }

    public FailureMessageDetails getFailureMessageDetails() {
        return Dsl.GenericFailureMessageDetails.NO_FAILURE_DETAILS;
    }

    @Override
    public String getExceptionMessageFromFailure() {
        return this.failureMessage;
    }

}
