package com.salesforce.vador.execution

internal data class VadorBean(val value: Int)

internal data class VadorRecursiveBean(val id: Int, val recursiveBeans: List<VadorRecursiveBean>?)
