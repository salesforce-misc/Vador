/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.validators.simple;


import consumer.bean.Parent;
import consumer.failure.ValidationFailure;
import org.revcloud.vader.types.validators.SimpleValidator;

import static consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

public class BaseParentRequestValidator {
    /**
     * Validates if Auth id in request has a status PROCESSED.
     * This is a lambda function implementation.
     */
    public static final SimpleValidator<Parent, ValidationFailure> validation1 =
            parent -> {
                if (parent.getMember() == null) {
                    return null;
                } else {
                    return new ValidationFailure(FIELD_NULL_OR_EMPTY);
                }
            };

    static final SimpleValidator<Parent, ValidationFailure> validation2 =
            parent -> {
                if (parent.getMember() == null) {
                    return null;
                } else {
                    return new ValidationFailure(FIELD_NULL_OR_EMPTY);
                }
            };
}
