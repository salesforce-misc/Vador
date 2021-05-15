package org.revcloud.vader.runner

import com.force.swag.id.ID
import com.force.swag.id.IdTraits
import de.cronn.reflection.util.PropertyUtils
import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function2
import io.vavr.Tuple2
import lombok.NonNull
import org.revcloud.vader.runner.SpecFactory.BaseSpec
import org.revcloud.vader.types.validators.Validator
import java.util.stream.Collectors

internal fun <ValidatableT, FailureT, FieldT> toValidators1(
    fieldMapperToFailure: Map<out TypedPropertyGetter<in ValidatableT, out FieldT>, FailureT>,
    fieldEval: (FieldT) -> Boolean
): List<Validator<ValidatableT, FailureT>> = fieldMapperToFailure.entries.map { (idFieldMapper, failure) ->
    Validator {
        it.map(idFieldMapper::get).filterOrElse(fieldEval) { failure }
    }
}

internal fun <ValidatableT, FailureT, FieldT> toValidators2(
    fieldMappersToFailure: Tuple2<out Collection<TypedPropertyGetter<in ValidatableT, out FieldT>>, @NonNull Function2<String, FieldT, FailureT>>?,
    fieldEval: (FieldT) -> Boolean
): List<Validator<ValidatableT, FailureT>> = fieldMappersToFailure?.let { (fieldMappers, failureFn) ->
    fieldMappers?.map { fieldMapper ->
        Validator { validatable ->
            validatable.map(fieldMapper::get).filterOrElse(fieldEval) { fieldValue ->
                failureFn?.apply(PropertyUtils.getPropertyName(validatable.get(), fieldMapper), fieldValue)
            }
        }
    }
} ?: emptyList()

internal fun <ValidatableT, FailureT> toValidators(validationConfig: BaseValidationConfig<ValidatableT, FailureT>): List<Validator<ValidatableT?, FailureT?>> =
    (toValidators1(validationConfig.shouldHaveFieldsOrFailWith, isFieldPresent) +
            toValidators2(validationConfig.shouldHaveFieldsOrFailWithFn, isFieldPresent) +
            toValidators1(validationConfig.shouldHaveValidSFIdFormatOrFailWith, isSFIdPresentAndValidFormat) +
            toValidators2(validationConfig.shouldHaveValidSFIdFormatOrFailWithFn, isSFIdPresentAndValidFormat) +
            toValidators1(validationConfig.absentOrHaveValidSFIdFieldsOrFailWith, isSFIdAbsentOrValidFormat) +
            toValidators2(validationConfig.absentOrHaveValidSFIdFormatOrFailWithFn, isSFIdAbsentOrValidFormat) +
            validationConfig.specs.map { it.toValidator() } +
            validationConfig.getValidators())

private fun <ValidatableT, FailureT> BaseSpec<ValidatableT, FailureT>.toValidator(): Validator<ValidatableT?, FailureT?> =
    Validator { it.filterOrElse(toPredicate()) { validatable -> getFailure(validatable) } }

val isFieldPresent: (Any?) -> Boolean = {
    when (it) {
        null -> false
        is String -> it.isNotBlank()
        else -> true
    }
}

val isSFIdPresentAndValidFormat: (ID?) -> Boolean = { it != null && IdTraits.isValidId(it.toString()) }

val isSFIdAbsentOrValidFormat: (ID?) -> Boolean = { it == null || IdTraits.isValidId(it.toString()) }

