/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.config;


import consumer.bean.BaseParent;
import consumer.failure.ValidationFailure;
import consumer.validators.batch.ParentBatchRequestValidator;
import consumer.validators.simple.BaseParentRequestValidator;
import consumer.validators.simple.ParentRequestValidator;
import io.vavr.collection.List;
import lombok.experimental.UtilityClass;
import org.revcloud.hyd.types.validators.SimpleValidator;
import org.revcloud.hyd.types.validators.Validator;


/**
 * gakshintala created on 4/13/20.
 */
@UtilityClass
public class ValidationConfig {
    
    public static List<Validator<BaseParent, ValidationFailure>> getServiceValidations() {
        return List.of(
                ParentBatchRequestValidator.batchValidation1,
                ParentBatchRequestValidator.batchValidation2);
    }

    public static List<SimpleValidator<? extends BaseParent, ValidationFailure>> getSimpleServiceValidations() {
        return List.of(BaseParentRequestValidator.validation1, ParentRequestValidator.validation1);
    }
}
