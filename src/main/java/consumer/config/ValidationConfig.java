/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.config;


import consumer.failure.ValidationFailure;
import consumer.representation.ParentInputRepresentation;
import consumer.validators.batch.ParentBatchRequestValidator;
import io.vavr.collection.List;
import org.qtc.delphinus.dsl.Dsl;
import org.qtc.delphinus.types.validators.Validator;


/**
 * gakshintala created on 4/13/20.
 */
public class ValidationConfig {
    
    List<Validator<ParentInputRepresentation, ValidationFailure>> getServiceRequestValidator() {
        return List.of(
                ParentBatchRequestValidator.batchValidation1,
                ParentBatchRequestValidator.batchValidation2)
                .appendAll(
                        Dsl.liftAllThrowable(List.of(
                                ParentBatchRequestValidator.batchValidationThrowable1,
                                ParentBatchRequestValidator.batchValidationThrowable2
                        ), ValidationFailure::getValidationFailureForException));
    }
}
