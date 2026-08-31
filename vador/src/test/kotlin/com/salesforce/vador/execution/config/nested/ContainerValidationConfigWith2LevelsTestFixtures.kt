package com.salesforce.vador.execution.config.nested

// tag::container-config-level-2[]
internal class ContainerValidationConfigWith2LevelsBean {
  override fun equals(other: Any?): Boolean = other is ContainerValidationConfigWith2LevelsBean

  override fun hashCode(): Int = 1

  override fun toString(): String = "ContainerValidationConfigWith2LevelsBean()"
}

// end::container-config-level-2[]

// tag::container-config-level-2[]
internal data class ContainerValidationConfigWith2LevelsContainerLevel2(
  val id: Int,
  val beanBatch: List<ContainerValidationConfigWith2LevelsBean>?,
)

// end::container-config-level-2[]

// tag::container-config-level-2[]
internal data class ContainerValidationConfigWith2LevelsContainerLevel1(
  val id: Int,
  val containerLevel2Batch: List<ContainerValidationConfigWith2LevelsContainerLevel2>?,
)

// end::container-config-level-2[]

// tag::container-config-level-2[]
internal data class ContainerValidationConfigWith2LevelsContainerRoot(
  val containerLevel1Batch: List<ContainerValidationConfigWith2LevelsContainerLevel1>?
)

// end::container-config-level-2[]

internal open class ContainerValidationConfigWith2LevelsContainerLevel1WithMultiBatch(
  open val containerLevel2Batch: List<ContainerValidationConfigWith2LevelsContainerLevel2>?,
  open val beanBatch: List<ContainerValidationConfigWith2LevelsBean>?,
) {
  object Fields {
    const val containerLevel2Batch = "containerLevel2Batch"
    const val beanBatch = "beanBatch"
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      (other is ContainerValidationConfigWith2LevelsContainerLevel1WithMultiBatch &&
        containerLevel2Batch == other.containerLevel2Batch &&
        beanBatch == other.beanBatch)

  override fun hashCode(): Int = listOf(containerLevel2Batch, beanBatch).hashCode()

  override fun toString(): String =
    "ContainerValidationConfigWith2LevelsContainerLevel1WithMultiBatch(" +
      "containerLevel2Batch=$containerLevel2Batch, beanBatch=$beanBatch)"
}

internal open class ContainerValidationConfigWith2LevelsContainerRootWithMultiContainerBatch(
  open val containerLevel1Batch1:
    List<ContainerValidationConfigWith2LevelsContainerLevel1WithMultiBatch>?,
  open val containerLevel1Batch2:
    List<ContainerValidationConfigWith2LevelsContainerLevel1WithMultiBatch>?,
) {
  object Fields {
    const val containerLevel1Batch1 = "containerLevel1Batch1"
    const val containerLevel1Batch2 = "containerLevel1Batch2"
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      (other is ContainerValidationConfigWith2LevelsContainerRootWithMultiContainerBatch &&
        containerLevel1Batch1 == other.containerLevel1Batch1 &&
        containerLevel1Batch2 == other.containerLevel1Batch2)

  override fun hashCode(): Int = listOf(containerLevel1Batch1, containerLevel1Batch2).hashCode()

  override fun toString(): String =
    "ContainerValidationConfigWith2LevelsContainerRootWithMultiContainerBatch(" +
      "containerLevel1Batch1=$containerLevel1Batch1, " +
      "containerLevel1Batch2=$containerLevel1Batch2)"
}
