package com.salesforce.vador.execution.config.nested

internal open class FieldConfigBean(
  open val requiredField1: Int?,
  open val requiredField2: String?,
  open val requiredList: List<String>?,
) {
  object Fields {
    const val requiredField1 = "requiredField1"
    const val requiredField2 = "requiredField2"
    const val requiredList = "requiredList"
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      (other is FieldConfigBean &&
        requiredField1 == other.requiredField1 &&
        requiredField2 == other.requiredField2 &&
        requiredList == other.requiredList)

  override fun hashCode(): Int = listOf(requiredField1, requiredField2, requiredList).hashCode()

  override fun toString(): String =
    "FieldConfigBean(" +
      "requiredField1=$requiredField1, requiredField2=$requiredField2, " +
      "requiredList=$requiredList)"
}
