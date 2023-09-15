package com.salesforce.vador.annotation

import com.salesforce.vador.types.ValidatorAnnotation1
import java.lang.reflect.Field
import java.time.LocalDate

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class PastDateField(val failureKey: String)

class PastDateFieldValidator<FailureT> : ValidatorAnnotation1<LocalDate, FailureT> {
  override fun validate(
    field: Field,
    value: LocalDate,
    failure: FailureT,
    none: FailureT
  ): FailureT {
    val todayDate = LocalDate.now()
    // input date should not be null/empty or equal to or greater than current date
    if (value == null || value.isEqual(todayDate) || value.isAfter(todayDate)) {
      return failure
    }
    return none
  }
}
