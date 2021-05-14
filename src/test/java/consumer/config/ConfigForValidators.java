/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.config;


import consumer.bean.Parent;
import consumer.bean.Container;
import consumer.failure.ValidationFailure;
import consumer.validators.batch.BaseParentBatchRequestValidator;
import consumer.validators.simple.BaseParentRequestValidator;
import consumer.validators.simple.ContainerRequestValidator;
import io.vavr.collection.List;
import lombok.experimental.UtilityClass;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;


/**
 * gakshintala created on 4/13/20.
 */
@UtilityClass
public class ConfigForValidators {
    
    public static List<Validator<Parent, ValidationFailure>> getServiceValidations() {
        return List.of(
                BaseParentBatchRequestValidator.batchValidation1,
                BaseParentBatchRequestValidator.batchValidation2);
    }

    public static List<Validator<Container, ValidationFailure>> getParentValidations() {
        return null;   
    }

    public static List<SimpleValidator<Container, ValidationFailure>> getParentSimpleValidations() {
        return List.of(
                ContainerRequestValidator.validation1,
                ContainerRequestValidator.validation2);
    }

    public static List<SimpleValidator<? extends Parent, ValidationFailure>> getSimpleServiceValidations() {
        return List.of(BaseParentRequestValidator.validation1, ContainerRequestValidator.validation1);
    }
}
