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

package sample.consumer.config;

import io.vavr.collection.List;
import org.revcloud.vador.types.ValidatorEtr;
import sample.consumer.BaseService;
import sample.consumer.Service;
import sample.consumer.bean.Parent;
import sample.consumer.failure.ValidationFailure;

/** gakshintala created on 4/13/20. */
public class ServiceConfig {

  BaseService<Parent> getService(List<ValidatorEtr<Parent, ValidationFailure>> requestValidators) {
    final Service service = new Service();
    service.setRequestValidators(requestValidators);
    return service;
  }
}
