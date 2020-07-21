/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.validators.batch;


import consumer.failure.ValidationFailure;
import consumer.bean.Child;
import io.vavr.control.Either;
import org.qtc.delphinus.types.validators.ThrowableValidator;
import org.qtc.delphinus.types.validators.Validator;
import consumer.failure.ValidationFailureMessage;

import java.util.Objects;

import static consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

public class ChildBatchRequestValidator {

    static final String ERROR_LABEL_PARAM_PAYMENT_AUTHORIZATION_ID
            = "PaymentStandardFields.PaymentAuthorizationId.getName()";
    /**
     * Validates if Auth id in request has a status PROCESSED.
     * This is a lambda function implementation.
     */
    public static final Validator<Child, ValidationFailure> batchValidation1 =
            child -> child
                    .filter(Objects::isNull)
                    .getOrElse(Either.left(new ValidationFailure(FIELD_NULL_OR_EMPTY)));

    public static final Validator<Child, ValidationFailure> batchValidation2 =
            child -> child
                    .filterOrElse(Objects::isNull, ignore -> new ValidationFailure(FIELD_NULL_OR_EMPTY));


    public static final ThrowableValidator<Child, ValidationFailure> batchValidationThrowable1 =
            childInputRepresentation -> {
                throw new IllegalArgumentException("1 - you did something illegal");
            };

    public static final ThrowableValidator<Child, ValidationFailure> batchValidationThrowable2 =
            childInputRepresentation -> {
                throw new IllegalArgumentException("1 - you did something illegal");
            };

}
