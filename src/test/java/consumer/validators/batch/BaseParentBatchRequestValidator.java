/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.validators.batch;


import consumer.bean.BaseParent;
import consumer.failure.ValidationFailure;
import org.revcloud.vader.types.validators.Validator;

import static consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

public class BaseParentBatchRequestValidator {
    
    /**
     * Validates if Auth id in request has a status PROCESSED.
     * This is a lambda function implementation.
     */
    public static final Validator<BaseParent, ValidationFailure> batchValidation1 =
            parentInputRepresentation -> parentInputRepresentation
                    .filterOrElse(parent -> parent.getChild() != null, ignore -> new ValidationFailure(FIELD_NULL_OR_EMPTY));

    public static final Validator<BaseParent, ValidationFailure> batchValidation2 =
            parentInputRepresentation -> parentInputRepresentation
                    .filterOrElse(parent -> parent.getChild() != null, ignore -> new ValidationFailure(FIELD_NULL_OR_EMPTY));

}
