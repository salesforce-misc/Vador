/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.config;


import consumer.failure.ValidationFailure;
import consumer.representation.Parent;
import consumer.validators.batch.ParentBatchRequestValidator;
import io.vavr.collection.List;
import lombok.experimental.UtilityClass;
import org.qtc.delphinus.dsl.Dsl;
import org.qtc.delphinus.types.validators.Validator;


/**
 * gakshintala created on 4/13/20.
 */
@UtilityClass
public class ValidationConfig {
    
    public static List<Validator<Parent, ValidationFailure>> getServiceValidations() {
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
