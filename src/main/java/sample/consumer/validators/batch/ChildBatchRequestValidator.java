/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package sample.consumer.validators.batch;


import sample.consumer.failure.ValidationFailure;
import sample.consumer.representation.ChildInputRepresentation;
import io.vavr.control.Either;
import org.qtc.delphinus.types.validators.ThrowableValidator;
import org.qtc.delphinus.types.validators.Validator;
import sample.consumer.failure.ApiErrorCodes;
import sample.consumer.failure.ValidationFailureMessage;

public class ChildBatchRequestValidator {

    static final String ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID
            = "PaymentStandardFields.PaymentAuthorizationId.getName()";
    /**
     * Validates if Auth id in request has a status PROCESSED.
     * This is a lambda function implementation.
     */
    public static final Validator<ChildInputRepresentation, ValidationFailure> batchValidation1 =
            childInputRepresentation -> childInputRepresentation
                    .filter(ChildInputRepresentation::_isSetAccountId)
                    .getOrElse(Either.left(new ValidationFailure(ApiErrorCodes.REQUIRED_FIELD_MISSING, ValidationFailureMessage.FIELD_NULL_OR_EMPTY,
                                                         ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID)));

    public static final Validator<ChildInputRepresentation, ValidationFailure> batchValidation2 =
            childInputRepresentation -> childInputRepresentation
                    .filter(ChildInputRepresentation::_isSetAccountId)
                    .getOrElse(Either.left(new ValidationFailure(ApiErrorCodes.REQUIRED_FIELD_MISSING, ValidationFailureMessage.FIELD_NULL_OR_EMPTY,
                            ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID)));


    public static final ThrowableValidator<ChildInputRepresentation, ValidationFailure> batchValidationThrowable1 =
            childInputRepresentation -> {
                throw new IllegalArgumentException("1 - you did something illegal");
            };

    public static final ThrowableValidator<ChildInputRepresentation, ValidationFailure> batchValidationThrowable2 =
            childInputRepresentation -> {
                throw new IllegalArgumentException("1 - you did something illegal");
            };

}
