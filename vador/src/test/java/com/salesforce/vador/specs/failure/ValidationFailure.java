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

import java.util.Objects;

/**
 * Reference Validation Failure
 *
 * @author gakshintala
 * @since 228
 */
public class ValidationFailure {
	public static final ValidationFailure NONE = new ValidationFailure(ValidationFailureMessage.NONE);
	public static final ValidationFailure INVALID_VALUE =
			new ValidationFailure(ValidationFailureMessage.INVALID_VALUE);
	private final ValidationFailureMessage validationFailureMessage;
	private String exceptionMsg;

	public ValidationFailure(ValidationFailureMessage validationFailureMessage) {
		this.validationFailureMessage = validationFailureMessage;
	}

	public ValidationFailureMessage getValidationFailureMessage() {
		return validationFailureMessage;
	}

	public String getExceptionMsg() {
		return exceptionMsg;
	}

	public void setExceptionMsg(String exceptionMsg) {
		this.exceptionMsg = exceptionMsg;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ValidationFailure validationFailure)
				|| !validationFailure.canEqual(this)) {
			return false;
		}
		return getValidationFailureMessage() == validationFailure.getValidationFailureMessage()
				&& Objects.equals(getExceptionMsg(), validationFailure.getExceptionMsg());
	}

	protected boolean canEqual(Object other) {
		return other instanceof ValidationFailure;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getValidationFailureMessage(), getExceptionMsg());
	}

	@Override
	public String toString() {
		return "ValidationFailure(validationFailureMessage="
				+ validationFailureMessage
				+ ", exceptionMsg="
				+ exceptionMsg
				+ ")";
	}
}
