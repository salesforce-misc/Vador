package com.salesforce.vador.annotation

import com.salesforce.vador.execution.strategies.util.isFieldPresent
import com.salesforce.vador.types.ValidatorAnnotation1
import java.lang.reflect.Field
import java.util.*

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Required(val failureKey: String)

class RequiredValidator<T, FailureT> : ValidatorAnnotation1<T?, FailureT> {
  override fun validate(field: Field, value: T?, failure: FailureT, none: FailureT): FailureT {
    if (isFieldPresent(value)) return none else return failure
  }
}
