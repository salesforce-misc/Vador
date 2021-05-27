/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.config;

import consumer.bean.Container;
import consumer.bean.Parent;
import consumer.failure.ValidationFailure;
import consumer.validators.batch.BaseParentBatchRequestValidator;
import consumer.validators.simple.BaseParentRequestValidator;
import consumer.validators.simple.ContainerRequestValidator;
import io.vavr.collection.List;
import lombok.experimental.UtilityClass;
import org.revcloud.vader.types.validators.Validator;
import org.revcloud.vader.types.validators.ValidatorEtr;

/** gakshintala created on 4/13/20. */
@UtilityClass
public class ConfigForValidators {

  public static List<ValidatorEtr<Parent, ValidationFailure>> getServiceValidations() {
    return List.of(
        BaseParentBatchRequestValidator.batchValidation1,
        BaseParentBatchRequestValidator.batchValidation2);
  }

  public static List<ValidatorEtr<Container, ValidationFailure>> getParentValidations() {
    return null;
  }

  public static List<Validator<Container, ValidationFailure>> getParentSimpleValidations() {
    return List.of(ContainerRequestValidator.validation1, ContainerRequestValidator.validation2);
  }

  public static List<Validator<? extends Parent, ValidationFailure>> getSimpleServiceValidations() {
    return List.of(BaseParentRequestValidator.validation1, ContainerRequestValidator.validation1);
  }
}
