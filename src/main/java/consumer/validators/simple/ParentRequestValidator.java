/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.validators.simple;


import consumer.failure.ValidationFailure;
import consumer.representation.ParentInputRepresentation;
import org.qtc.delphinus.types.validators.SimpleThrowableValidator;
import org.qtc.delphinus.types.validators.SimpleValidator;

import static consumer.failure.ApiErrorCodes.REQUIRED_FIELD_MISSING;
import static consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

public class ParentRequestValidator {

    static final String ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID
            = "PaymentStandardFields.PaymentAuthorizationId.getName()";
    /**
     * Validates if Auth id in request has a status PROCESSED.
     * This is a lambda function implementation.
     */
    public static final SimpleValidator<ParentInputRepresentation, ValidationFailure> validation1 =
            parentInputRepresentation -> {
                if (parentInputRepresentation._isSetPaymentAuthorizationId()) {
                    return null;
                } else {
                    return new ValidationFailure(REQUIRED_FIELD_MISSING, FIELD_NULL_OR_EMPTY,
                            ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID);
                }
            };

    static final SimpleValidator<ParentInputRepresentation, ValidationFailure> validation2 =
            parentInputRepresentation -> {
                if (parentInputRepresentation._isSetPaymentAuthorizationId()) {
                    return null;
                } else {
                    return new ValidationFailure(REQUIRED_FIELD_MISSING, FIELD_NULL_OR_EMPTY,
                            ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID);
                }
            };

    static final SimpleThrowableValidator<ParentInputRepresentation, ValidationFailure> validationThrowable1 =
            parentInputRepresentation -> {
                throw new IllegalArgumentException("1 - you did something illegal");
            };

    static final SimpleThrowableValidator<ParentInputRepresentation, ValidationFailure> validationThrowable2 =
            parentInputRepresentation -> {
                throw new IllegalArgumentException("2 - you did something illegal again");
            };

}
