package com.salesforce.vador.annotation

import com.salesforce.vador.types.ValidatorAnnotation

class Negative<FailureT> : ValidatorAnnotation<FailureT> {
  override fun validate(value: Any, failure: FailureT, none: FailureT): FailureT {
    val intValue =
      when (value) {
        is Int -> value
        else -> throw RuntimeException("Value is not an integer")
      }
    if (intValue > 0) {
      return failure
    }
    return none
  }
}
