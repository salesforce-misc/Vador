/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.config;


import consumer.bean.BaseParent;
import consumer.bean.Child;
import consumer.bean.Parent;
import consumer.failure.ValidationFailure;
import consumer.validators.batch.BaseParentBatchRequestValidator;
import consumer.validators.batch.ParentBatchRequestValidator;
import consumer.validators.simple.BaseParentRequestValidator;
import consumer.validators.simple.ParentRequestValidator;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;


/**
 * gakshintala created on 4/13/20.
 */
@UtilityClass
public class ValidationConfig {
    
    public static List<Validator<BaseParent, ValidationFailure>> getServiceValidations() {
        return List.of(
                BaseParentBatchRequestValidator.batchValidation1,
                BaseParentBatchRequestValidator.batchValidation2);
    }

    public static List<Validator<Parent, ValidationFailure>> getParentValidations() {
        return null;   
    }

    public static List<SimpleValidator<Parent, ValidationFailure>> getParentSimpleValidations() {
        return List.of(
                ParentRequestValidator.validation1,
                ParentRequestValidator.validation2);
    }

    public static List<SimpleValidator<? extends BaseParent, ValidationFailure>> getSimpleServiceValidations() {
        return List.of(BaseParentRequestValidator.validation1, ParentRequestValidator.validation1);
    }
}
