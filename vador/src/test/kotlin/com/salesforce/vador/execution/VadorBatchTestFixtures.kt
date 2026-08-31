package com.salesforce.vador.execution

internal data class VadorBatchBean(val id: Int)

internal data class VadorBatchRecursiveBean(
  val id: Int,
  val recursiveBeans: List<VadorBatchRecursiveBean>?,
)
