package com.salesforce.vador.execution.config

internal class ContainerValidationConfigBean1 {
  override fun equals(other: Any?): Boolean = other is ContainerValidationConfigBean1

  override fun hashCode(): Int = 1

  override fun toString(): String = "ContainerValidationConfigBean1()"
}

internal class ContainerValidationConfigBean2 {
  override fun equals(other: Any?): Boolean = other is ContainerValidationConfigBean2

  override fun hashCode(): Int = 1

  override fun toString(): String = "ContainerValidationConfigBean2()"
}

internal open class ContainerValidationConfigContainerWithMultiBatch(
  open val batch1: List<ContainerValidationConfigBean1>?,
  open val batch2: List<ContainerValidationConfigBean2>?,
) {
  object Fields {
    const val batch1 = "batch1"
    const val batch2 = "batch2"
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      (other is ContainerValidationConfigContainerWithMultiBatch &&
        batch1 == other.batch1 &&
        batch2 == other.batch2)

  override fun hashCode(): Int = listOf(batch1, batch2).hashCode()

  override fun toString(): String =
    "ContainerValidationConfigContainerWithMultiBatch(batch1=$batch1, batch2=$batch2)"
}

internal class ContainerValidationConfigBean {
  override fun equals(other: Any?): Boolean = other is ContainerValidationConfigBean

  override fun hashCode(): Int = 1

  override fun toString(): String = "ContainerValidationConfigBean()"
}

internal data class ContainerValidationConfigContainerLevel1(
  val beanBatch: List<ContainerValidationConfigBean>?
)

internal data class ContainerValidationConfigContainerRoot(
  val containerLevel1Batch: List<ContainerValidationConfigContainerLevel1>?
)

internal data class ContainerValidationConfigContainerWithPair(
  val id: Int,
  val beanBatch: List<ContainerValidationConfigBean>?,
)
