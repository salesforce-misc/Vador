package com.salesforce.vador.execution.config.nested

internal data class BatchOfBatch1ValidationConfigBean(val value: Int, val label: String?)

internal data class BatchOfBatch1ValidationConfigItem(
  val id: String?,
  val beanBatch: List<BatchOfBatch1ValidationConfigBean>?,
)

internal data class BatchOfBatch1ValidationConfigRoot(
  val itemsBatch: List<BatchOfBatch1ValidationConfigItem>?
)
