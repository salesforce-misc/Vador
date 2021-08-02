package org.revcloud.vader.runner

import com.force.swag.id.ID
import com.force.swag.id.IdTraits
import de.cronn.reflection.util.PropertyUtils
import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function2
import io.vavr.Tuple2
import io.vavr.control.Either
import org.revcloud.vader.runner.SpecFactory.BaseSpec
import org.revcloud.vader.types.validators.ValidatorEtr

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

private fun <FailureT, FieldT, ValidatableT> applyFailureFn(
  failureFn: Function2<String, FieldT, FailureT>?,
  validatable: Either<FailureT?, ValidatableT?>,
  fieldMapper: TypedPropertyGetter<in ValidatableT, out FieldT>
): (FieldT) -> FailureT? = { fieldValue: FieldT ->
  failureFn?.apply(PropertyUtils.getPropertyName(validatable.get(), fieldMapper), fieldValue)
}

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
internal fun <ValidatableT, FailureT> toValidators(
  validationConfig: BaseValidationConfig<ValidatableT, FailureT>
): List<ValidatorEtr<ValidatableT?, FailureT?>> = (
  toValidatorEtrs1(validationConfig.shouldHaveFieldsOrFailWith, isFieldPresent) +
    toValidatorEtrs2(validationConfig.shouldHaveFieldsOrFailWithFn, isFieldPresent) +
    toValidatorEtrs3(validationConfig.shouldHaveFieldOrFailWithFn, isFieldPresent) +
    toValidatorEtrs1(validationConfig.shouldHaveValidSFIdFormatForAllOrFailWith, isSFIdPresentAndValidFormat) +
    toValidatorEtrs2(validationConfig.shouldHaveValidSFIdFormatForAllOrFailWithFn, isSFIdPresentAndValidFormat) +
    toValidatorEtrs3(validationConfig.shouldHaveValidSFIdFormatOrFailWithFn, isSFIdPresentAndValidFormat) +
    toValidatorEtrs1(validationConfig.absentOrHaveValidSFIdFormatForAllOrFailWith, isSFIdAbsentOrValidFormat) +
    toValidatorEtrs2(validationConfig.absentOrHaveValidSFIdFormatForAllOrFailWithFn, isSFIdAbsentOrValidFormat) +
    toValidatorEtrs3(validationConfig.absentOrHaveValidSFIdFormatOrFailWithFn, isSFIdAbsentOrValidFormat) +
    validationConfig.specs.map { it.toValidator() } +
    validationConfig.getValidators()
  )

@JvmSynthetic
private fun <ValidatableT, FailureT> BaseSpec<ValidatableT, FailureT>.toValidator(): ValidatorEtr<ValidatableT?, FailureT?> =
  ValidatorEtr { it.filterOrElse(toPredicate()) { validatable -> getFailure(validatable) } }

@JvmSynthetic
private val isFieldPresent: (Any?) -> Boolean = {
  when (it) {
    null -> false
    is String -> it.isNotBlank()
    is List<*> -> it.isNotEmpty()
    else -> true
  }
}

@JvmSynthetic
private val isSFIdPresentAndValidFormat: (ID?) -> Boolean =
  { it != null && IdTraits.isValidIdStrictChecking(it.toString(), true) }

@JvmSynthetic
private val isSFIdAbsentOrValidFormat: (ID?) -> Boolean =
  { it == null || IdTraits.isValidIdStrictChecking(it.toString(), true) }
