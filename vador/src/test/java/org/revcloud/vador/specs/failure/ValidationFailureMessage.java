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

package org.revcloud.vador.specs.failure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This enum holds all localized representations of all Service validation Failures.
 *
 * @author gakshintala
 * @since 220
 */
@RequiredArgsConstructor
@Getter
@ToString
public enum ValidationFailureMessage {
  NONE(Section.COMMON_VALIDATION_FAILURE, "Success"),
  INVALID_VALUE("", "InvalidValue"),
  ;

  private final String section;
  private final String name;
  @Setter private Object[] params;

  private static final class Section {
    static final String COMMON_VALIDATION_FAILURE = "CommonValidationFailure";
  }
}
