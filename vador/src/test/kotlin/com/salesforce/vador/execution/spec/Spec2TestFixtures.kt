package com.salesforce.vador.execution.spec

internal enum class Spec2BillingTerm {
  OneTime,
  Month,
}

internal data class Spec2Bean(
  val value: Int?,
  val valueStr: String?,
  val dependentValue1: Int?,
  val dependentValue2: Int?,
)

internal data class Spec2Bean2(val bt: Spec2BillingTerm?, val valueStr: String?)
