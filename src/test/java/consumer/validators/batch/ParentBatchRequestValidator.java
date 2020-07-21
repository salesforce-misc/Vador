/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.validators.batch;


import consumer.failure.ApiErrorCodes;
import consumer.failure.ValidationFailure;
import consumer.representation.Parent;
import io.vavr.control.Either;
import org.qtc.delphinus.types.validators.ThrowableValidator;
import org.qtc.delphinus.types.validators.Validator;

import static consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

public class ParentBatchRequestValidator {

    static final String ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID
            = "PaymentStandardFields.PaymentAuthorizationId.getName()";
    /**
     * Validates if Auth id in request has a status PROCESSED.
     * This is a lambda function implementation.
     */
    public static final Validator<Parent, ValidationFailure> batchValidation1 =
            parentInputRepresentation -> parentInputRepresentation
                    .filterOrElse(Parent::_isSetAccountId, ignore -> new ValidationFailure(
                            ApiErrorCodes.REQUIRED_FIELD_MISSING,
                            FIELD_NULL_OR_EMPTY,
                            ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID));

    public static final Validator<Parent, ValidationFailure> batchValidation2 =
            parentInputRepresentation -> parentInputRepresentation
                    .filterOrElse(Parent::_isSetAccountId, ignore -> new ValidationFailure(
                            ApiErrorCodes.REQUIRED_FIELD_MISSING,
                            FIELD_NULL_OR_EMPTY,
                            ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID));

    public static final ThrowableValidator<Parent, ValidationFailure> batchValidationThrowable1 =
            parentInputRepresentation -> {
                throw new IllegalArgumentException("1 - you did something illegal");
            };

    public static final ThrowableValidator<Parent, ValidationFailure> batchValidationThrowable2 =
            parentInputRepresentation -> {
                throw new IllegalArgumentException("1 - you did something illegal");
            };

}
