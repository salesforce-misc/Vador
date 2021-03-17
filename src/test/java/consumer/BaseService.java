/*
 * Copyright 2020 salesforce.com, inc. 
 * All Rights Reserved 
 * Company Confidential
 */

package consumer;


import consumer.config.ValidationConfig;
import consumer.failure.ValidationFailure;
import consumer.bean.Parent;
import io.vavr.collection.List;
import lombok.val;
import org.qtc.delphinus.dsl.runner.RunnerDsl;
import org.qtc.delphinus.types.validators.Validator;

/**
 * gakshintala created on 4/13/20.
 */
public abstract class BaseService<InputRepresentationT> {
    List<Validator<InputRepresentationT, ValidationFailure>> requestValidators;

    public void setRequestValidators(List<Validator<InputRepresentationT, ValidationFailure>> requestValidators) {
        this.requestValidators = requestValidators;
    }

    public ValidationFailure validateNonBulk() {
        val parentInputRepresentation = new Parent(0, null);
        return RunnerDsl.validateAndFailFast(
                parentInputRepresentation,
                ValidationConfig.getServiceValidations(),
                ValidationFailure.NOTHING_TO_VALIDATE,
                ValidationFailure.NONE,
                throwable -> ValidationFailure.NONE);
    }
}
