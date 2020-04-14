/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package org.qtc.delphinus;


import com.google.common.collect.ImmutableList;

/**
 * gakshintala created on 4/9/20.
 */
public final class Dsl {

    private Dsl() {
    }

    public static <FailureT, RequestT> RequestValidator<FailureT, RequestT> noRequestValidation() {
        return new RequestValidator<>(ImmutableList.of());
    }

    public enum GenericValidationFailureMessage implements ValidationFailureMessage {
        NONE,
        UNKNOWN_EXCEPTION,
        ;

        @Override
        public String getSection() {
            return "";
        }

        @Override
        public String getName() {
            return "";
        }
    }
    
}        
