/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus;


import com.google.common.collect.ImmutableList;
import org.qtc.delphinus.failure.Failure;

/**
 * gakshintala created on 4/9/20.
 */
public final class Dsl {

    private Dsl() {
    }

    public static <FailureT, RequestT> RequestValidator<FailureT, RequestT> noRequestValidation() {
        return new RequestValidator<>(ImmutableList.of());
    }

    private static final FailureMessageDetails NO_FAILURE_DETAILS = new FailureMessageDetails("", "");

    public static final Failure NONE = new Failure() {
        @Override
        public FailureMessageDetails getFailureMessageDetails() {
            return NO_FAILURE_DETAILS;
        }

        @Override
        public String getExceptionMessageFromFailure() {
            return "";
        }
    };

}        
