package com.salesforce.vador.execution

// tag::batch-bean[]
internal data class UtilsBean(val id: String?)

// end::batch-bean[]

// tag::batch-bean-multikey[]
internal data class UtilsMultiKeyBean(val id1: String?, val id2: String?)
// end::batch-bean-multikey[]
