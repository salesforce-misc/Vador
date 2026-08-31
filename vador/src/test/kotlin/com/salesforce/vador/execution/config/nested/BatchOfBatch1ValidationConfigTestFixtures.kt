package com.salesforce.vador.execution.config.nested

// tag::batch-of-batch-1[]
internal data class BatchOfBatch1ValidationConfigBean(val value: Int, val label: String?)

// end::batch-of-batch-1[]

// tag::batch-of-batch-1[]
internal data class BatchOfBatch1ValidationConfigItem(
  val id: String?,
  val beanBatch: List<BatchOfBatch1ValidationConfigBean>?,
)

// end::batch-of-batch-1[]

// tag::batch-of-batch-1[]
internal data class BatchOfBatch1ValidationConfigRoot(
  val itemsBatch: List<BatchOfBatch1ValidationConfigItem>?
)
// end::batch-of-batch-1[]
