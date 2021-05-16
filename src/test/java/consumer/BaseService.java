/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer;

import consumer.failure.ValidationFailure;
import io.vavr.collection.List;
import org.revcloud.vader.types.validators.Validator;

/** gakshintala created on 4/13/20. */
public abstract class BaseService<InputRepresentationT> {
  List<Validator<InputRepresentationT, ValidationFailure>> requestValidators;

  public void setRequestValidators(
      List<Validator<InputRepresentationT, ValidationFailure>> requestValidators) {
    this.requestValidators = requestValidators;
  }
}
