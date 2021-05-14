/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.validators.batch;


import consumer.bean.Container;
import consumer.failure.ValidationFailure;
import org.revcloud.vader.types.validators.Validator;

import static consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

public class ContainerBatchRequestValidator {
    
    /**
     * Validates if Auth id in request has a status PROCESSED.
     * This is a lambda function implementation.
     */
    public static final Validator<Container, ValidationFailure> batchValidation1 =
            containerInputRepresentation -> containerInputRepresentation
                    .filterOrElse(container -> container.getMember() != null, ignore -> new ValidationFailure(FIELD_NULL_OR_EMPTY));

    public static final Validator<Container, ValidationFailure> batchValidation2 =
            containerInputRepresentation -> containerInputRepresentation
                    .filterOrElse(container -> container.getMember() != null, ignore -> new ValidationFailure(FIELD_NULL_OR_EMPTY));

}
