/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package sample.consumer.validators.batch;


import sample.consumer.failure.ValidationFailure;
import sample.consumer.representation.ParentInputRepresentation;
import io.vavr.control.Either;
import org.qtc.delphinus.types.validators.ThrowableValidator;
import org.qtc.delphinus.types.validators.Validator;
import sample.consumer.failure.ApiErrorCodes;

import static sample.consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

public class ParentBatchRequestValidator {

    static final String ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID
            = "PaymentStandardFields.PaymentAuthorizationId.getName()";
    /**
     * Validates if Auth id in request has a status PROCESSED.
     * This is a lambda function implementation.
     */
    public static final Validator<ParentInputRepresentation, ValidationFailure> batchValidation1 =
            parentInputRepresentation -> parentInputRepresentation
                    .filter(ParentInputRepresentation::_isSetAccountId)
                    .getOrElse(Either.left(new ValidationFailure(ApiErrorCodes.REQUIRED_FIELD_MISSING, FIELD_NULL_OR_EMPTY,
                                                         ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID)));

    public static final Validator<ParentInputRepresentation, ValidationFailure> batchValidation2 =
            parentInputRepresentation -> parentInputRepresentation
                    .filter(ParentInputRepresentation::_isSetAccountId)
                    .getOrElse(Either.left(new ValidationFailure(ApiErrorCodes.REQUIRED_FIELD_MISSING, FIELD_NULL_OR_EMPTY,
                            ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID)));


    public static final ThrowableValidator<ParentInputRepresentation, ValidationFailure> batchValidationThrowable1 =
            parentInputRepresentation -> {
                throw new IllegalArgumentException("1 - you did something illegal");
            };

    public static final ThrowableValidator<ParentInputRepresentation, ValidationFailure> batchValidationThrowable2 =
            parentInputRepresentation -> {
                throw new IllegalArgumentException("1 - you did something illegal");
            };

}
