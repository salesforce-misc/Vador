package com.salesforce.vador.annotation

import com.salesforce.vador.types.ValidatorAnnotation2
import java.lang.reflect.Field

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class MinForInt(val limit: Int, val failureKey: String)

class MinForIntValidator<FailureT> : ValidatorAnnotation2<Int, FailureT> {
  override fun validate(
    field: Field,
    value1: Int,
    value2: Int,
    failure: FailureT,
    none: FailureT
  ): FailureT {
    if (value1 < value2) {
      return failure
    }
    return none
  }
}
