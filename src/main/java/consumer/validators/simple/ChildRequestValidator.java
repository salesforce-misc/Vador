/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.validators.simple;


import consumer.failure.ValidationFailure;
import consumer.representation.ChildInputRepresentation;
import org.qtc.delphinus.types.validators.SimpleThrowableValidator;
import org.qtc.delphinus.types.validators.SimpleValidator;

import static consumer.failure.ApiErrorCodes.REQUIRED_FIELD_MISSING;
import static consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

public class ChildRequestValidator {

    static final String ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID
            = "PaymentStandardFields.PaymentAuthorizationId.getName()";
    /**
     * Validates if Auth id in request has a status PROCESSED.
     * This is a lambda function implementation.
     */
    public static final SimpleValidator<ChildInputRepresentation, ValidationFailure> validation1 =
            childInputRepresentation -> {
                if (childInputRepresentation._isSetPaymentAuthorizationId()) {
                    return null;
                } else {
                    return new ValidationFailure(REQUIRED_FIELD_MISSING, FIELD_NULL_OR_EMPTY,
                            ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID);
                }
            };

    static final SimpleValidator<ChildInputRepresentation, ValidationFailure> validation2 =
            childInputRepresentation -> {
                if (childInputRepresentation._isSetPaymentAuthorizationId()) {
                    return null;
                } else {
                    return new ValidationFailure(REQUIRED_FIELD_MISSING, FIELD_NULL_OR_EMPTY,
                            ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID);
                }
            };

    static final SimpleThrowableValidator<ChildInputRepresentation, ValidationFailure> validationThrowable1 =
            childInputRepresentation -> {
                throw new IllegalArgumentException("1 - you did something illegal");
            };

    static final SimpleThrowableValidator<ChildInputRepresentation, ValidationFailure> validationThrowable2 =
            childInputRepresentation -> {
                throw new IllegalArgumentException("2 - you did something illegal again");
            };

}
