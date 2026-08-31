package com.salesforce.vador.execution.spec

import java.util.Date
import kotlin.jvm.JvmName

internal data class Spec3DatesBean(
  @get:JvmName("isCompareDates") val compareDates: Boolean,
  val date1: Date?,
  val date2: Date?,
)

internal data class Spec3Bean(
  @get:JvmName("isCompareFields") val compareFields: Boolean,
  val bdom: Int?,
  val startDate: Date?,
)
