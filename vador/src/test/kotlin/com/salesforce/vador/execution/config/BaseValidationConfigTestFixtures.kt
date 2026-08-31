package com.salesforce.vador.execution.config

import java.util.Optional

internal open class BaseValidationConfigBeanWithIdStrFields(
  open val requiredField: String?,
  open val accountId: String?,
  open val contactId: String?,
) {
  object Fields {
    const val requiredField = "requiredField"
    const val accountId = "accountId"
    const val contactId = "contactId"
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      (other is BaseValidationConfigBeanWithIdStrFields &&
        requiredField == other.requiredField &&
        accountId == other.accountId &&
        contactId == other.contactId)

  override fun hashCode(): Int = listOf(requiredField, accountId, contactId).hashCode()

  override fun toString(): String =
    "BaseValidationConfigBeanWithIdStrFields(" +
      "requiredField=$requiredField, accountId=$accountId, contactId=$contactId)"
}

internal open class BaseValidationConfigBean(
  open val requiredField1: Int?,
  open val requiredField2: String?,
  open val sfId1: String?,
  open val sfId2: String?,
  open val requiredList: List<String>?,
) {
  object Fields {
    const val requiredField1 = "requiredField1"
    const val requiredField2 = "requiredField2"
    const val sfId1 = "sfId1"
    const val sfId2 = "sfId2"
    const val requiredList = "requiredList"
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      (other is BaseValidationConfigBean &&
        requiredField1 == other.requiredField1 &&
        requiredField2 == other.requiredField2 &&
        sfId1 == other.sfId1 &&
        sfId2 == other.sfId2 &&
        requiredList == other.requiredList)

  override fun hashCode(): Int =
    listOf(requiredField1, requiredField2, sfId1, sfId2, requiredList).hashCode()

  override fun toString(): String =
    "BaseValidationConfigBean(" +
      "requiredField1=$requiredField1, requiredField2=$requiredField2, " +
      "sfId1=$sfId1, sfId2=$sfId2, requiredList=$requiredList)"
}

internal data class BaseValidationConfigContainerBean(
  val requiredField: String?,
  val bean: BaseValidationConfigBean?,
)

internal open class BaseValidationConfigBean1(open val str: Optional<String>?) {
  object Fields {
    const val str = "str"
  }

  override fun equals(other: Any?): Boolean =
    this === other || (other is BaseValidationConfigBean1 && str == other.str)

  override fun hashCode(): Int = str?.hashCode() ?: 0

  override fun toString(): String = "BaseValidationConfigBean1(str=$str)"
}
