/*
 * Copyright 2020 salesforce.com, inc.
 * All Rights Reserved
 * Company Confidential
 */

package sample.consumer.failure;

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
  INVALID_BEAN(Section.COMMON_VALIDATION_FAILURE, "InvalidBean"),
  INVALID_BEAN_1(Section.COMMON_VALIDATION_FAILURE, "InvalidBean1"),
  INVALID_BEAN_2(Section.COMMON_VALIDATION_FAILURE, "InvalidBean2"),
  INVALID_PARENT(Section.COMMON_VALIDATION_FAILURE, "InvalidParent"),
  INVALID_ITEM(Section.COMMON_VALIDATION_FAILURE, "InvalidItem"),
  INVALID_UDD_ID(Section.COMMON_VALIDATION_FAILURE, "InvalidUDDId"),
  INVALID_OPTIONAL_UDD_ID(Section.COMMON_VALIDATION_FAILURE, "InvalidOptionalUddId"),
  INVALID_UDD_ID_2(Section.COMMON_VALIDATION_FAILURE, "InvalidUDDId2"),
  INVALID_UDD_ID_3(Section.COMMON_VALIDATION_FAILURE, "InvalidUDDId3"),
  INVALID_CONTAINER(Section.COMMON_VALIDATION_FAILURE, "InvalidContainer"),
  INVALID_CHILD(Section.COMMON_VALIDATION_FAILURE, "InvalidChild"),
  INVALID_MEMBER(Section.COMMON_VALIDATION_FAILURE, "InvalidMember"),
  UNKNOWN_EXCEPTION("", ""),
  VALIDATION_FAILURE_1("", ""),
  VALIDATION_FAILURE_2("", ""),
  VALIDATION_FAILURE_3("", ""),
  REQUIRED_FIELD_MISSING("", ""),
  REQUIRED_LIST_MISSING("", ""),
  REQUIRED_FIELD_MISSING_1("", ""),
  REQUIRED_FIELD_MISSING_2("", ""),
  INVALID_COMBO_1("", ""),
  INVALID_COMBO_2("", ""),
  INVALID_VALUE("", ""),
  FIELD_INTEGRITY_EXCEPTION("", ""),
  MIN_BATCH_SIZE_EXCEEDED("", ""),
  MIN_BATCH_SIZE_NOT_MET("", "MinBatchSizeNotMet"),
  MIN_BATCH_SIZE_NOT_MET_ROOT_LEVEL("", "MinBatchSizeNotMetRootLevel"),
  MAX_NESTED_BATCH_SIZE_EXCEEDED_2("", "MaxNestedBatchSizeExceeded2"),
  MIN_BATCH_SIZE_NOT_MET_LEVEL_1("", ""),
  MAX_BATCH_SIZE_EXCEEDED("", ""),
  MIN_BATCH_SIZE_NOT_MET_LEVEL_2("", ""),
  MAX_BATCH_SIZE_EXCEEDED_LEVEL_2("", ""),
  MAX_BATCH_SIZE_EXCEEDED_LEVEL_3("", ""),
  MSG_WITH_PARAMS("", ""),
  ;

  private final String section;
  private final String name;
  @Setter private Object[] params;

  private static final class Section {
    static final String COMMON_VALIDATION_FAILURE = "CommonValidationFailure";
  }
}
