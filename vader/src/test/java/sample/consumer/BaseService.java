/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package sample.consumer;

import io.vavr.collection.List;
import org.revcloud.vader.types.validators.ValidatorEtr;
import sample.consumer.failure.ValidationFailure;

/** gakshintala created on 4/13/20. */
public abstract class BaseService<InputRepresentationT> {

  List<ValidatorEtr<InputRepresentationT, ValidationFailure>> requestValidators;

  public void setRequestValidators(
      List<ValidatorEtr<InputRepresentationT, ValidationFailure>> requestValidators) {
    this.requestValidators = requestValidators;
  }
}
