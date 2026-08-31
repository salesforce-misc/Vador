package com.salesforce.vador.execution.spec

internal data class Spec4Bean(
  val whenField1: Int,
  val whenField2: String?,
  val whenField3: Spec4Field?,
  val thenField1: Int,
  val thenField2: String?,
  val thenField3: Spec4Field?,
)

internal data class Spec4Field(val id: Int)
