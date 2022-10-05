/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package sample.consumer;

import com.salesforce.vador.types.ValidatorEtr;
import io.vavr.collection.List;
import sample.consumer.failure.ValidationFailure;

/** gakshintala created on 4/13/20. */
public abstract class BaseService<InputRepresentationT> {

  List<ValidatorEtr<InputRepresentationT, ValidationFailure>> requestValidators;

  public void setRequestValidators(
      List<ValidatorEtr<InputRepresentationT, ValidationFailure>> requestValidators) {
    this.requestValidators = requestValidators;
  }
}
