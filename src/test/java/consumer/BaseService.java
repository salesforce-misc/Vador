/*
 * Copyright 2020 salesforce.com, inc. 
 * All Rights Reserved 
 * Company Confidential
 */

package consumer;


import consumer.config.ValidationConfig;
import consumer.failure.ValidationFailure;
import consumer.representation.ParentInputRepresentation;
import io.vavr.collection.List;
import lombok.val;
import org.qtc.delphinus.dsl.ValidateDsl;
import org.qtc.delphinus.types.validators.Validator;

/**
 * gakshintala created on 4/13/20.
 */
public abstract class BaseService<InputRepresentationT extends ConnectInputRepresentation> {
    List<Validator<InputRepresentationT, ValidationFailure>> requestValidators;

    public void setRequestValidators(List<Validator<InputRepresentationT, ValidationFailure>> requestValidators) {
        this.requestValidators = requestValidators;
    }

    public ValidationFailure validateNonBulk() {
        val parentInputRepresentation = new ParentInputRepresentation();
        return ValidateDsl.validateAndFailFast(
                parentInputRepresentation,
                ValidationConfig.getServiceValidations(),
                ValidationFailure.NOTHING_TO_VALIDATE,
                ValidationFailure.SUCCESS);
    }
}
