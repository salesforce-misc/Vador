package com.salesforce.vador.execution

import com.salesforce.vador.annotation.MaxForInt
import com.salesforce.vador.annotation.MinForInt
import com.salesforce.vador.annotation.Negative
import com.salesforce.vador.annotation.NonNegative
import com.salesforce.vador.annotation.Positive
import com.salesforce.vador.annotation.Required
import com.salesforce.vador.annotation.TestAnnotation
import com.salesforce.vador.annotation.ValidateWith

internal data class AnnotationBean(
  @field:Positive(failureKey = "unexpectedException") val idOne: Int,
  @field:Negative(failureKey = "unexpectedException") val idTwo: Int,
  @field:NonNegative(failureKey = "unexpectedException") val idThree: Int,
)

internal data class BeanMix(
  @field:Positive(failureKey = "unexpectedException") val idOne: Int,
  @field:Negative(failureKey = "unexpectedException") val idTwo: String?,
)

internal data class BeanCustom(
  @field:ValidateWith(
    validator = VadorAnnotationTest.myIdValidator1::class,
    failureKey = "unexpectedException",
  )
  val idOne: VadorAnnotationTest.ID?
)

internal data class BeanCustom2(
  @field:ValidateWith(
    validator = VadorAnnotationTest.myIdValidator1::class,
    failureKey = "unexpectedException",
  )
  val idOne: VadorAnnotationTest.ID?,
  @field:ValidateWith(
    validator = VadorAnnotationTest.myIdValidator2::class,
    failureKey = "unexpectedException",
  )
  val idTwo: VadorAnnotationTest.ID?,
)

internal data class BeanCustom3(
  @field:ValidateWith(
    validator = VadorAnnotationTest.myIdValidator3::class,
    failureKey = "unexpectedException",
  )
  val idOne: VadorAnnotationTest.ID?
)

internal data class BeanInt(
  @field:MaxForInt(limit = 100, failureKey = "unexpectedException") val idOne: Int,
  @field:MinForInt(limit = 500, failureKey = "unexpectedException") val idTwo: Int,
)

internal data class BeanFailure(@field:TestAnnotation(testParam = 100) val idOne: Int)

internal data class BeanRequired<T>(
  @field:Required(failureKey = "unexpectedException") val idOne: T?
)
