package com.salesforce.vador.execution.strategies

import com.salesforce.vador.annotation.ValidateWith
import com.salesforce.vador.lift.liftToEtr
import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorAnnotation1
import com.salesforce.vador.types.ValidatorEtr
import io.vavr.Tuple2
import java.lang.reflect.InvocationTargetException
import org.apache.commons.lang3.reflect.FieldUtils

object AnnotationProcessorBase {
  @Throws(
    NoSuchMethodException::class,
    InvocationTargetException::class,
    InstantiationException::class,
    IllegalAccessException::class,
  )
  fun <ValidatableT, FailureT> derivedValidators(
    bean: ValidatableT,
    mapOfAnnotation: Tuple2<MutableMap<String, FailureT>, FailureT>?
  ): List<ValidatorEtr<ValidatableT?, FailureT?>> {
    val fields = bean!!::class.java.declaredFields
    val map = mapOfAnnotation?._1()
    val none = mapOfAnnotation?._2()
    return fields.mapNotNull { field ->
      if (field.isAnnotationPresent(ValidateWith::class.java)) {
        val annotation = field.getAnnotation(ValidateWith::class.java)
        val failureKey = annotation.failureKey
        if (ValidatorAnnotation1::class.java.isAssignableFrom(annotation.validator.java)) {
          val validatorAnnotation =
            annotation.validator.java.getDeclaredConstructor().newInstance()
              as ValidatorAnnotation1<Any, FailureT?>
          val validator =
            Validator<ValidatableT?, FailureT?> {
              validatorAnnotation.validate(
                field,
                FieldUtils.readField(field, bean, true) as Any,
                map?.get(failureKey),
                none,
              )
            }
          liftToEtr<FailureT, ValidatableT>(validator, none)
        } else {
          throw IllegalArgumentException("Provided Annotation is not supported.")
        }
      } else {
        null
      }
    }
  }
}
