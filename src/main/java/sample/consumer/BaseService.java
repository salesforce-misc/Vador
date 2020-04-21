/*
 * Copyright 2020 salesforce.com, inc. 
 * All Rights Reserved 
 * Company Confidential
 */

package sample.consumer;


import sample.consumer.config.ValidationConfig;
import sample.consumer.failure.ValidationFailure;
import sample.consumer.representation.ParentInputRepresentation;
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
        return ValidateDsl.nonBulkValidateFailFast(
                parentInputRepresentation,
                ValidationConfig.getServiceValidations(),
                ValidationFailure.NOTHING_TO_VALIDATE,
                ValidationFailure.SUCCESS);
    }
}
