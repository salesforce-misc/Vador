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

import java.util.Arrays;

/**
 * This enum holds all localized representations of all Service validation Failures.
 *
 * @author gakshintala
 * @since 220
 */
public enum ValidationFailureMessage {
	NONE(Section.COMMON_VALIDATION_FAILURE, "Success"),
	INVALID_VALUE("", "InvalidValue"),
	;

	private final String section;
	private final String name;
	private Object[] params;

	ValidationFailureMessage(String section, String name) {
		this.section = section;
		this.name = name;
	}

	public String getSection() {
		return section;
	}

	public String getName() {
		return name;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	@Override
	public String toString() {
		return name()
				+ "(section="
				+ section
				+ ", name="
				+ name
				+ ", params="
				+ Arrays.deepToString(params)
				+ ")";
	}

	private static final class Section {
		static final String COMMON_VALIDATION_FAILURE = "CommonValidationFailure";
	}
}
