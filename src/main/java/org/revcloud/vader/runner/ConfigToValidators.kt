package org.revcloud.vader.runner

import com.force.swag.id.ID
import com.force.swag.id.IdTraits
import de.cronn.reflection.util.PropertyUtils
import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function2
import io.vavr.Tuple2
import io.vavr.control.Either
import lombok.NonNull
import org.revcloud.vader.runner.SpecFactory.BaseSpec
import org.revcloud.vader.types.validators.Validator
import java.util.stream.Collectors
import java.util.stream.Stream

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

internal fun <ValidatableT, FailureT> toValidators(validationConfig: BaseValidationConfig<ValidatableT, FailureT>): Stream<Validator<ValidatableT, FailureT>> =
    (toValidators1(validationConfig.shouldHaveFieldsOrFailWith, isFieldPresent) +
            toValidators2(validationConfig.shouldHaveFieldsOrFailWithFn, isFieldPresent) +
            toValidators1(validationConfig.shouldHaveValidSFIdFormatOrFailWith, isSFIdPresentAndValidFormat) +
            toValidators2(validationConfig.shouldHaveValidSFIdFormatOrFailWithFn, isSFIdPresentAndValidFormat) +
            toValidators1(validationConfig.absentOrHaveValidSFIdFieldsOrFailWith, isSFIdAbsentOrValidFormat) +
            toValidators2(validationConfig.absentOrHaveValidSFIdFormatOrFailWithFn, isSFIdAbsentOrValidFormat) +
            validationConfig.specsStream.collect(Collectors.toList()).map { toValidator(it) } +
            validationConfig.getValidatorsStream().collect(Collectors.toList())).stream()


val isFieldPresent: (Any?) -> Boolean = {
    when (it) {
        null -> false
        is String -> it.isNotBlank()
        else -> true
    }
}

val isSFIdPresentAndValidFormat: (ID?) -> Boolean = { it != null && IdTraits.isValidId(it.toString()) }

val isSFIdAbsentOrValidFormat: (ID?) -> Boolean = { it == null || IdTraits.isValidId(it.toString()) }

private fun <ValidatableT, FailureT> toValidator(baseSpec: BaseSpec<ValidatableT, FailureT>): Validator<ValidatableT, FailureT> =
    Validator { validatableRight: Either<FailureT, ValidatableT> ->
        validatableRight.filterOrElse(
            baseSpec.toPredicate()
        ) { ignore: ValidatableT -> baseSpec.getFailure(ignore) }
    }

