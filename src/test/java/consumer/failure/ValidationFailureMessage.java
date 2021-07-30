/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package consumer.failure;

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
  FIELD_NULL_OR_EMPTY("", ""),
  NOTHING_TO_VALIDATE(Section.COMMON_VALIDATION_FAILURE, "Nothing"),
  DUPLICATE_ITEM(Section.COMMON_VALIDATION_FAILURE, "DuplicateItem"),
  DUPLICATE_ITEM_1(Section.COMMON_VALIDATION_FAILURE, "DuplicateItem1"),
  DUPLICATE_ITEM_2(Section.COMMON_VALIDATION_FAILURE, "DuplicateItem2"),
  NULL_KEY(Section.COMMON_VALIDATION_FAILURE, "NullKey"),
  INVALID_PARENT(Section.COMMON_VALIDATION_FAILURE, "invalidParent"),
  INVALID_CHILD(Section.COMMON_VALIDATION_FAILURE, "invalidChild"),
  UNKNOWN_EXCEPTION("", ""),
  VALIDATION_FAILURE_1("", ""),
  VALIDATION_FAILURE_2("", ""),
  VALIDATION_FAILURE_3("", ""),
  REQUIRED_FIELD_MISSING("", ""),
  INVALID_COMBO_1("", ""),
  INVALID_COMBO_2("", ""),
  INVALID_VALUE("", ""),
  FIELD_INTEGRITY_EXCEPTION("", ""),
  MIN_BATCH_SIZE_EXCEEDED("", ""),
  MAX_BATCH_SIZE_EXCEEDED("", ""),
  MSG_WITH_PARAMS("", ""),
  ;

  private final String section;
  private final String name;
  @Setter private Object[] params;

  private static final class Section {

    static final String COMMON_VALIDATION_FAILURE = "CommonValidationFailure";
  }
}
