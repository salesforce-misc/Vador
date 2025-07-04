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

package com.salesforce.vador.specs.failure;

import lombok.Data;

/**
 * Reference Validation Failure
 *
 * @author gakshintala
 * @since 228
 */
@Data
public class ValidationFailure {
	public static final ValidationFailure NONE = new ValidationFailure(ValidationFailureMessage.NONE);
	public static final ValidationFailure INVALID_VALUE =
			new ValidationFailure(ValidationFailureMessage.INVALID_VALUE);
	private final ValidationFailureMessage validationFailureMessage;
	private String exceptionMsg;
}
