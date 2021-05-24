package org.revcloud.vader.runner

import com.force.swag.id.ID
import com.force.swag.id.IdTraits
import de.cronn.reflection.util.PropertyUtils
import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function2
import io.vavr.Tuple2
import lombok.NonNull
import org.revcloud.vader.runner.SpecFactory.BaseSpec
import org.revcloud.vader.types.validators.ValidatorEtr

internal fun <ValidatableT, FailureT, FieldT> toValidatorEtrs1(
    fieldMapperToFailure: Map<out TypedPropertyGetter<in ValidatableT, out FieldT>, FailureT>,
    fieldEval: (FieldT) -> Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
    fieldMapperToFailure.entries.map { (idFieldMapper, failure) ->
        ValidatorEtr {
            it.map(idFieldMapper::get).filterOrElse(fieldEval) { failure }
        }
    }

internal fun <ValidatableT, FailureT, FieldT> toValidatorEtrs2(
    fieldMappersToFailure: Tuple2<out Collection<TypedPropertyGetter<in ValidatableT, out FieldT>>, @NonNull Function2<String, FieldT, FailureT>>?,
    fieldEval: (FieldT) -> Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
    fieldMappersToFailure?.let { (fieldMappers, failureFn) ->
        fieldMappers?.map { fieldMapper ->
            ValidatorEtr { validatable ->
                validatable.map(fieldMapper::get).filterOrElse(fieldEval) { fieldValue ->
                    failureFn?.apply(
                        PropertyUtils.getPropertyName(validatable.get(), fieldMapper),
                        fieldValue
                    )
                }
            }
        }
    } ?: emptyList()

internal fun <ValidatableT, FailureT> toValidators(validationConfig: BaseValidationConfig<ValidatableT, FailureT>): List<ValidatorEtr<ValidatableT?, FailureT?>> =
    (
        toValidatorEtrs1(validationConfig.shouldHaveFieldsOrFailWith, isFieldPresent) +
            toValidatorEtrs2(validationConfig.shouldHaveFieldsOrFailWithFn, isFieldPresent) +
            toValidatorEtrs1(
                validationConfig.shouldHaveValidSFIdFormatOrFailWith,
                isSFIdPresentAndValidFormat
            ) +
            toValidatorEtrs2(
                validationConfig.shouldHaveValidSFIdFormatOrFailWithFn,
                isSFIdPresentAndValidFormat
            ) +
            toValidatorEtrs1(
                validationConfig.absentOrHaveValidSFIdFieldsOrFailWith,
                isSFIdAbsentOrValidFormat
            ) +
            toValidatorEtrs2(
                validationConfig.absentOrHaveValidSFIdFormatOrFailWithFn,
                isSFIdAbsentOrValidFormat
            ) +
            validationConfig.specs.map { it.toValidator() } +
            validationConfig.getValidators()
        )

private fun <ValidatableT, FailureT> BaseSpec<ValidatableT, FailureT>.toValidator(): ValidatorEtr<ValidatableT?, FailureT?> =
    ValidatorEtr { it.filterOrElse(toPredicate()) { validatable -> getFailure(validatable) } }

val isFieldPresent: (Any?) -> Boolean = {
    when (it) {
        null -> false
        is String -> it.isNotBlank()
        else -> true
    }
}

val isSFIdPresentAndValidFormat: (ID?) -> Boolean =
    { it != null && IdTraits.isValidId(it.toString()) }

val isSFIdAbsentOrValidFormat: (ID?) -> Boolean =
    { it == null || IdTraits.isValidId(it.toString()) }
