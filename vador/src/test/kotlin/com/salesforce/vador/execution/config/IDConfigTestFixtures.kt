package com.salesforce.vador.execution.config

// tag::bean-with-id-fields[]
internal open class IDConfigBeanWithIdFields2(
  open val accountId: IDConfigID?,
  open val contactId: IDConfigID?,
) {
  object Fields {
    const val accountId = "accountId"
    const val contactId = "contactId"
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      (other is IDConfigBeanWithIdFields2 &&
        accountId == other.accountId &&
        contactId == other.contactId)

  override fun hashCode(): Int = listOf(accountId, contactId).hashCode()

  override fun toString(): String =
    "IDConfigBeanWithIdFields2(accountId=$accountId, contactId=$contactId)"
}

internal class IDConfigAccountEntityId : IDConfigTest.EntityId {
  override fun equals(other: Any?): Boolean = other is IDConfigAccountEntityId

  override fun hashCode(): Int = 1

  override fun toString(): String = "IDConfigAccountEntityId()"
}

internal class IDConfigContactEntityId : IDConfigTest.EntityId {
  override fun equals(other: Any?): Boolean = other is IDConfigContactEntityId

  override fun hashCode(): Int = 1

  override fun toString(): String = "IDConfigContactEntityId()"
}

// end::bean-with-id-fields[]

internal class IDConfigProductEntityId : IDConfigTest.EntityId {
  override fun equals(other: Any?): Boolean = other is IDConfigProductEntityId

  override fun hashCode(): Int = 1

  override fun toString(): String = "IDConfigProductEntityId()"
}

internal open class IDConfigBeanWithIdFields3(
  open val accountId: IDConfigID?,
  open val contactId: IDConfigID?,
  open val productId: IDConfigID?,
) {
  object Fields {
    const val accountId = "accountId"
    const val contactId = "contactId"
    const val productId = "productId"
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      (other is IDConfigBeanWithIdFields3 &&
        accountId == other.accountId &&
        contactId == other.contactId &&
        productId == other.productId)

  override fun hashCode(): Int = listOf(accountId, contactId, productId).hashCode()

  override fun toString(): String =
    "IDConfigBeanWithIdFields3(" +
      "accountId=$accountId, contactId=$contactId, productId=$productId)"
}

internal open class IDConfigBeanWithPolymorphicIdFields(open val accountOrContactId: IDConfigID?) {
  object Fields {
    const val accountOrContactId = "accountOrContactId"
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      (other is IDConfigBeanWithPolymorphicIdFields &&
        accountOrContactId == other.accountOrContactId)

  override fun hashCode(): Int = accountOrContactId?.hashCode() ?: 0

  override fun toString(): String =
    "IDConfigBeanWithPolymorphicIdFields(accountOrContactId=$accountOrContactId)"
}

internal open class IDConfigBeanWithMixIdFields(
  open val requiredField: String?,
  open val accountId: IDConfigID?,
  open val contactId: String?,
) {
  object Fields {
    const val requiredField = "requiredField"
    const val accountId = "accountId"
    const val contactId = "contactId"
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      (other is IDConfigBeanWithMixIdFields &&
        requiredField == other.requiredField &&
        accountId == other.accountId &&
        contactId == other.contactId)

  override fun hashCode(): Int = listOf(requiredField, accountId, contactId).hashCode()

  override fun toString(): String =
    "IDConfigBeanWithMixIdFields(" +
      "requiredField=$requiredField, accountId=$accountId, contactId=$contactId)"
}

internal data class IDConfigID(val value: String?)
