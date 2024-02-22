package com.salesforce.vador.execution.strategies

import com.salesforce.vador.annotation.MaxForInt
import com.salesforce.vador.annotation.MaxForIntValidator
import com.salesforce.vador.annotation.MinForInt
import com.salesforce.vador.annotation.MinForIntValidator
import com.salesforce.vador.annotation.Negative
import com.salesforce.vador.annotation.NegativeValidator
import com.salesforce.vador.annotation.NonNegative
import com.salesforce.vador.annotation.NonNegativeValidator
import com.salesforce.vador.annotation.Positive
import com.salesforce.vador.annotation.PositiveValidator
import com.salesforce.vador.annotation.Required
import com.salesforce.vador.annotation.RequiredValidator
import com.salesforce.vador.annotation.ValidateWith
import com.salesforce.vador.lift.liftToEtr
import com.salesforce.vador.types.Validator
import com.salesforce.vador.types.ValidatorAnnotation1
import com.salesforce.vador.types.ValidatorAnnotation2
import com.salesforce.vador.types.ValidatorEtr
import io.vavr.Tuple2
import java.lang.reflect.InvocationTargetException
import org.apache.commons.lang3.reflect.FieldUtils

object AnnotationProcessor {
  @Throws(
    NoSuchMethodException::class,
    InvocationTargetException::class,
    InstantiationException::class,
    IllegalAccessException::class,
  )
  fun <ValidatableT, FailureT> deriveValidators(
    bean: ValidatableT,
    mapOfAnnotation: Tuple2<MutableMap<String, FailureT>, FailureT>?
  ): List<ValidatorEtr<ValidatableT?, FailureT?>> {
    val fields = bean!!::class.java.declaredFields
    val map = mapOfAnnotation?._1()
    val none = mapOfAnnotation?._2()
    return fields.mapNotNull { field ->
      if (field.annotations.isNotEmpty()) {
        val validator: Validator<ValidatableT?, FailureT?> =
          when (field.annotations[0].annotationClass) {
            ValidateWith::class -> {
              val annotation = field.getAnnotation(ValidateWith::class.java)
              when {
                ValidatorAnnotation1::class.java.isAssignableFrom(annotation.validator.java) -> {
                  val validatorAnnotation =
                    annotation.validator.java.getDeclaredConstructor().newInstance()
                      as ValidatorAnnotation1<Any, FailureT?>
                  Validator {
                    validatorAnnotation.validate(
                      field,
                      FieldUtils.readField(field, bean, true) as Any,
                      map?.get(annotation.failureKey),
                      none,
                    )
                  }
                }
                else -> {
                  throw IllegalArgumentException(
                    "Provided Annotation is not supported. Please use supported annotations or custom annotations with valid instance. For more info please check official documentation."
                  )
                }
              }
            }
            Negative::class -> {
              val negativeValidator =
                NegativeValidator::class.java.getDeclaredConstructor().newInstance()
                  as ValidatorAnnotation1<Any?, FailureT?>
              Validator {
                negativeValidator.validate(
                  field,
                  FieldUtils.readField(field, bean, true) as Any?,
                  map?.get(field.getAnnotation(Negative::class.java).failureKey),
                  none,
                )
              }
            }
            NonNegative::class -> {
              val nonNegativeValidator =
                NonNegativeValidator::class.java.getDeclaredConstructor().newInstance()
                  as ValidatorAnnotation1<Any?, FailureT?>
              Validator {
                nonNegativeValidator.validate(
                  field,
                  FieldUtils.readField(field, bean, true) as Any?,
                  map?.get(field.getAnnotation(NonNegative::class.java).failureKey),
                  none,
                )
              }
            }
            Positive::class -> {
              val positiveValidator =
                PositiveValidator::class.java.getDeclaredConstructor().newInstance()
                  as ValidatorAnnotation1<Any?, FailureT?>
              Validator {
                positiveValidator.validate(
                  field,
                  FieldUtils.readField(field, bean, true) as Any?,
                  map?.get(field.getAnnotation(Positive::class.java).failureKey),
                  none,
                )
              }
            }
            MaxForInt::class -> {
              val annotation = field.getAnnotation(MaxForInt::class.java)
              val maxIntValidator =
                MaxForIntValidator::class.java.getDeclaredConstructor().newInstance()
                  as ValidatorAnnotation2<Any, FailureT?>
              Validator {
                maxIntValidator.validate(
                  field,
                  FieldUtils.readField(field, bean, true) as Any,
                  annotation.limit,
                  map?.get(annotation.failureKey),
                  none,
                )
              }
            }
            MinForInt::class -> {
              val annotation = field.getAnnotation(MinForInt::class.java)
              val minIntValidator =
                MinForIntValidator::class.java.getDeclaredConstructor().newInstance()
                  as ValidatorAnnotation2<Any, FailureT?>
              Validator {
                minIntValidator.validate(
                  field,
                  FieldUtils.readField(field, bean, true) as Any,
                  annotation.limit,
                  map?.get(annotation.failureKey),
                  none,
                )
              }
            }
            Required::class -> {
              val annotation = field.getAnnotation(Required::class.java)
              val requiredValidator =
                RequiredValidator::class.java.getDeclaredConstructor().newInstance()
                  as ValidatorAnnotation1<Any?, FailureT?>
              Validator {
                requiredValidator.validate(
                  field,
                  FieldUtils.readField(field, bean, true) as Any?,
                  map?.get(annotation.failureKey),
                  none,
                )
              }
            }
            else -> Validator { null }
          }
        liftToEtr<FailureT, ValidatableT>(validator, none)
      } else {
        null
      }
    }
  }
}
