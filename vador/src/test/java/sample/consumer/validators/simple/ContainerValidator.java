/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

/*
 * Copyright 2019 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package sample.consumer.validators.simple;

import static sample.consumer.failure.ValidationFailureMessage.FIELD_NULL_OR_EMPTY;

import com.salesforce.vador.types.Validator;
import sample.consumer.bean.Container;
import sample.consumer.failure.ValidationFailure;

public class ContainerValidator {

	public static final Validator<Container, ValidationFailure> validator1 =
			container -> {
				if (container.getMember() == null) {
					return new ValidationFailure(FIELD_NULL_OR_EMPTY);
				} else {
					return ValidationFailure.NONE;
				}
			};

	public static final Validator<Container, ValidationFailure> validator2 =
			container -> {
				if (container.getMember() == null) {
					return new ValidationFailure(FIELD_NULL_OR_EMPTY);
				} else {
					return ValidationFailure.NONE;
				}
			};
}
