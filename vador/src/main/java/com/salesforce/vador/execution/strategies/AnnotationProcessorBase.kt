package com.salesforce.vador.execution.strategies

import com.salesforce.vador.annotation.ValidateWith
import com.salesforce.vador.lift.liftToEtr
import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorAnnotation
import com.salesforce.vador.types.ValidatorEtr
import io.vavr.Tuple2
import java.lang.reflect.Field
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
    bean: Any?,
    mapOfAnnotation: Tuple2<MutableMap<String, FailureT>, FailureT>?
  ): List<ValidatorEtr<ValidatableT?, FailureT?>> {
    val listOfValidatorEtr: MutableList<ValidatorEtr<ValidatableT?, FailureT?>> = ArrayList()
    val fields = bean?.let { getFields(it.javaClass) }
    if (fields != null) {
      for (field in fields) {
        var validator: Validator<ValidatableT?, FailureT?>? = null
        val map = mapOfAnnotation?._1()
        val none = mapOfAnnotation?._2()
        if (field.isAnnotationPresent(ValidateWith::class.java)) {
          val annotation =
            field.getAnnotation(
              ValidateWith::class.java,
            )
          val failureKey = annotation.failureKey
          if (ValidatorAnnotation::class.java.isAssignableFrom(annotation.validator.java)) {
            val validatorAnnotation =
              annotation.validator.java.getDeclaredConstructor().newInstance()
                as ValidatorAnnotation<FailureT?>
            validator = Validator {
              validatorAnnotation.validate(
                FieldUtils.readField(field, bean, true),
                map?.get(failureKey),
                none,
              )
            }
          }
          if (validator != null) {
            listOfValidatorEtr.add(liftToEtr<FailureT, ValidatableT>(validator, none))
          }
        }
      }
    }
    return listOfValidatorEtr
  }

  private fun getFields(classRef: Class<*>): Array<Field> {
    return classRef.declaredFields
  }
}
