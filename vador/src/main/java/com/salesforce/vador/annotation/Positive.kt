package com.salesforce.vador.annotation

import com.salesforce.vador.types.ValidatorAnnotation1
import java.lang.reflect.Field

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Positive(val failureKey: String)

class PositiveValidator<FailureT> : ValidatorAnnotation1<Int, FailureT> {
  override fun validate(field: Field, value: Int, failure: FailureT, none: FailureT): FailureT {
    if (value < 1) {
      return failure
    }
    return none
  }
}
