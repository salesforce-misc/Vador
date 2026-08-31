package com.salesforce.vador.execution.spec

internal data class Spec5Bean1(
  val whenField1: Int,
  val whenField2: String?,
  val whenField3: Spec5Field?,
  val thenField1: Int?,
  val thenField2: String?,
  val thenField3: Spec5Field?,
)

internal data class Spec5Bean2(
  val whenField1: Int,
  val whenField2: Int,
  val thenField1: String?,
  val thenField2: String?,
)

internal data class Spec5Field(val id: Int)
