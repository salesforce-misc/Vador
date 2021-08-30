package org.revcloud.vader.runner

import com.force.swag.id.ID
import com.force.swag.id.IdTraits
import de.cronn.reflection.util.PropertyUtils
import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function2
import io.vavr.Tuple2
import io.vavr.control.Either
import org.revcloud.vader.specs.specs.BaseSpec
import org.revcloud.vader.types.validators.ValidatorEtr
import java.util.Optional

@JvmSynthetic
private fun <ValidatableT, FailureT, FieldT> toValidatorEtrs1(
  fieldMapperToFailure: Map<out TypedPropertyGetter<in ValidatableT, out FieldT>, FailureT>,
  fieldEval: (FieldT) -> Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  fieldMapperToFailure.entries.map { (fieldMapper, failure) ->
    ValidatorEtr {
      it.map(fieldMapper::get).filterOrElse(fieldEval) { failure }
    }
  }

@JvmSynthetic
private fun <ValidatableT, FailureT, FieldT> toValidatorEtrs2(
  fieldMappersToFailure: Tuple2<out Collection<TypedPropertyGetter<in ValidatableT, out FieldT>>, Function2<String, FieldT, FailureT>>?,
  fieldEval: (FieldT) -> Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  fieldMappersToFailure?.let { (fieldMappers, failureFn) ->
    fieldMappers?.map { fieldMapper ->
      ValidatorEtr {
        it.map(fieldMapper::get).filterOrElse(fieldEval, applyFailureFn(failureFn, it, fieldMapper))
      }
    }
  } ?: emptyList()

@JvmSynthetic
private fun <ValidatableT, FailureT, FieldT> toValidatorEtrs3(
  fieldMapperToFailure: Map<out TypedPropertyGetter<in ValidatableT, out FieldT>, Function2<String, FieldT, FailureT>?>,
  fieldEval: (FieldT) -> Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  fieldMapperToFailure.entries.map { (fieldMapper, failureFn) ->
    ValidatorEtr {
      it.map(fieldMapper::get).filterOrElse(fieldEval, applyFailureFn(failureFn, it, fieldMapper))
    }
  }

@JvmSynthetic
private fun <ValidatableT, FailureT, EntityInfoT> idConfigToValidatorEtrs(
  config: IDConfig<ValidatableT, FailureT, EntityInfoT>?,
  fieldEvalFallback: (ID) -> Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.shouldHaveValidSFIdFormatForAllOrFailWith?.mapNotNull { (fieldMapper, entityInfo, failure) ->
    fieldMapper?.let {
      ValidatorEtr { validatable ->
        validatable.map(fieldMapper::get)
          .filterOrElse(
            { field ->
              val fieldEval = config.withIdValidator
              when {
                fieldEval != null -> fieldEval.apply(field, entityInfo)
                else -> fieldEvalFallback(field)
              }
            }
          ) { failure }
      }
    }
  } ?: emptyList()

@JvmSynthetic
internal fun <ValidatableT, FailureT> toValidators(
  config: BaseValidationConfig<ValidatableT, FailureT>
): List<ValidatorEtr<ValidatableT?, FailureT?>> = (
  toValidatorEtrs1(config.shouldHaveFieldsOrFailWith, isFieldPresent) +
    toValidatorEtrs2(config.shouldHaveFieldsOrFailWithFn, isFieldPresent) +
    toValidatorEtrs3(config.shouldHaveFieldOrFailWithFn, isFieldPresent) +
    toValidatorEtrs1(config.shouldHaveValidSFIdFormatForAllOrFailWith, isSFIdPresentAndValidFormat) +
    toValidatorEtrs2(config.shouldHaveValidSFIdFormatForAllOrFailWithFn, isSFIdPresentAndValidFormat) +
    toValidatorEtrs3(config.shouldHaveValidSFIdFormatOrFailWithFn, isSFIdPresentAndValidFormat) +
    toValidatorEtrs1(config.absentOrHaveValidSFIdFormatForAllOrFailWith, isSFIdAbsentOrValidFormat) +
    toValidatorEtrs2(config.absentOrHaveValidSFIdFormatForAllOrFailWithFn, isSFIdAbsentOrValidFormat) +
    toValidatorEtrs3(config.absentOrHaveValidSFIdFormatOrFailWithFn, isSFIdAbsentOrValidFormat) +
    idConfigToValidatorEtrs(config.withIdConfig, isSFIdPresentAndValidFormat) +
    config.specs.map { it.toValidator() } +
    config.getValidators()
  )

private fun <FailureT, FieldT, ValidatableT> applyFailureFn(
  failureFn: Function2<String, FieldT, FailureT>?,
  validatable: Either<FailureT?, ValidatableT?>,
  fieldMapper: TypedPropertyGetter<in ValidatableT, out FieldT>
): (FieldT) -> FailureT? = { fieldValue: FieldT ->
  failureFn?.apply(PropertyUtils.getPropertyName(validatable.get(), fieldMapper), fieldValue)
}

@JvmSynthetic
private fun <ValidatableT, FailureT> BaseSpec<ValidatableT, FailureT>.toValidator(): ValidatorEtr<ValidatableT?, FailureT?> =
  ValidatorEtr { it.filterOrElse(toPredicate()) { validatable -> getFailure(validatable) } }

@JvmSynthetic
private val isFieldPresent: (Any?) -> Boolean = {
  when (it) {
    null -> false
    is String -> it.isNotBlank()
    is List<*> -> it.isNotEmpty()
    is Optional<*> -> it.isPresent
    else -> true
  }
}

@JvmSynthetic
private val isSFIdPresentAndValidFormat: (ID?) -> Boolean =
  { it != null && IdTraits.isValidIdStrictChecking(it.toString(), true) }

@JvmSynthetic
private val isSFIdAbsentOrValidFormat: (ID?) -> Boolean =
  { it == null || IdTraits.isValidIdStrictChecking(it.toString(), true) }
