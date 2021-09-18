package org.revcloud.vader.runner

import com.force.swag.id.ID
import com.force.swag.id.IdTraits
import de.cronn.reflection.util.PropertyUtils
import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function2
import io.vavr.Tuple2
import io.vavr.Tuple3
import io.vavr.control.Either
import org.revcloud.vader.specs.specs.BaseSpec
import org.revcloud.vader.types.validators.ValidatorEtr
import java.util.Optional

@JvmSynthetic
private fun <ValidatableT, FailureT, FieldT> toValidatorEtrs1(
  fieldMapperToFailure: Map<out TypedPropertyGetter<in ValidatableT, out FieldT>, FailureT>,
  fieldValidator: (FieldT) -> Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  fieldMapperToFailure.entries.map { (fieldMapper, failure) ->
    ValidatorEtr {
      it.map(fieldMapper::get).filterOrElse(fieldValidator) { failure }
    }
  }

@JvmSynthetic
private fun <ValidatableT, FailureT, FieldT> toValidatorEtrs2(
  fieldMappersToFailure: Tuple2<out Collection<TypedPropertyGetter<in ValidatableT, out FieldT>>, Function2<String, FieldT, FailureT>>?,
  fieldValidator: (FieldT) -> Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  fieldMappersToFailure?.let { (fieldMappers, failureFn) ->
    fieldMappers.map { fieldMapper ->
      ValidatorEtr { validatable ->
        validatable.map(fieldMapper::get)
          .filterOrElse(fieldValidator, applyFailureFn(failureFn, validatable, fieldMapper))
      }
    }
  } ?: emptyList()

@JvmSynthetic
private fun <ValidatableT, FailureT, FieldT> toValidatorEtrs3(
  fieldMapperToFailure: Map<out TypedPropertyGetter<in ValidatableT, out FieldT>, Function2<String, FieldT, FailureT>?>,
  fieldValidator: (FieldT) -> Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  fieldMapperToFailure.entries.map { (fieldMapper, failureFn) ->
    ValidatorEtr { validatable ->
      validatable.map(fieldMapper::get)
        .filterOrElse(fieldValidator, applyFailureFn(failureFn, validatable, fieldMapper))
    }
  }

@JvmSynthetic
private fun <ValidatableT, FailureT, EntityInfoT> idConfigToValidatorEtrs(
  config: IDConfig<ValidatableT, FailureT, EntityInfoT>?,
  idValidatorFallback: (ID?) -> Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  toValidators11(
    config?.shouldHaveValidSFIdFormatForAllOrFailWith,
    config?.withIdValidator,
    idValidatorFallback,
    true
  ) +
    toValidators12(
      config?.shouldHaveValidSFIdFormatForAllOrFailWithFn,
      config?.withIdValidator,
      idValidatorFallback,
      true
    ) +
    toValidators13(
      config?.shouldHaveValidSFIdFormatOrFailWithFn,
      config?.withIdValidator,
      idValidatorFallback,
      true
    ) +
    toValidators11(
      config?.absentOrHaveValidSFIdFormatForAllOrFailWith,
      config?.withIdValidator,
      idValidatorFallback,
      false
    ) +
    toValidators12(
      config?.absentOrHaveValidSFIdFormatForAllOrFailWithFn,
      config?.withIdValidator,
      idValidatorFallback,
      false
    ) +
    toValidators13(
      config?.absentOrHaveValidSFIdFormatOrFailWithFn,
      config?.withIdValidator,
      idValidatorFallback,
      false
    )

@JvmSynthetic
private fun <EntityInfoT, FailureT, ValidatableT> toValidators11(
  config: Collection<Tuple3<TypedPropertyGetter<ValidatableT, ID?>, out EntityInfoT, FailureT?>>?,
  idValidator: Function2<ID, EntityInfoT, Boolean>?,
  idValidatorFallback: (ID?) -> Boolean,
  optionalId: Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.map { (idFieldMapper, entityInfo, failure) ->
    ValidatorEtr { validatable ->
      validatable.map(idFieldMapper::get)
        .filterOrElse(
          validateId(idValidator, entityInfo, idValidatorFallback, optionalId)
        ) { failure }
    }
  } ?: emptyList()

@JvmSynthetic
private fun <EntityInfoT, FailureT, ValidatableT> toValidators12(
  config: Tuple3<Collection<TypedPropertyGetter<ValidatableT, ID?>>, out EntityInfoT, Function2<String, ID?, FailureT?>>?,
  idValidator: Function2<ID, EntityInfoT, Boolean>?,
  idValidatorFallback: (ID?) -> Boolean,
  optionalId: Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.let { (idFieldMappers, entityInfo, failureFn) ->
    idFieldMappers.map { idFieldMapper ->
      ValidatorEtr { validatable ->
        validatable.map(idFieldMapper::get)
          .filterOrElse(
            validateId(idValidator, entityInfo, idValidatorFallback, optionalId),
            applyFailureFn(failureFn, validatable, idFieldMapper)
          )
      }
    }
  } ?: emptyList()

@JvmSynthetic
private fun <EntityInfoT, FailureT, ValidatableT> toValidators13(
  config: Collection<Tuple3<TypedPropertyGetter<ValidatableT, ID?>, out EntityInfoT, Function2<String, ID?, FailureT?>>>?,
  idValidator: Function2<ID, EntityInfoT, Boolean>?,
  idValidatorFallback: (ID?) -> Boolean,
  optionalId: Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.map { (idFieldMapper, entityInfo, failureFn) ->
    ValidatorEtr { validatable ->
      validatable.map(idFieldMapper::get)
        .filterOrElse(
          validateId(idValidator, entityInfo, idValidatorFallback, optionalId),
          applyFailureFn(failureFn, validatable, idFieldMapper)
        )
    }
  } ?: emptyList()

@JvmSynthetic
private fun <EntityInfoT> validateId(
  idValidator: Function2<ID, EntityInfoT, Boolean>?,
  entityInfo: EntityInfoT,
  idValidatorFallback: (ID?) -> Boolean,
  optionalId: Boolean
) = { id: ID? ->
  when {
    idValidator != null -> when {
      optionalId -> id != null && idValidator.apply(id, entityInfo)
      else -> id == null || idValidator.apply(id, entityInfo)
    }
    else -> idValidatorFallback(id)
  }
}

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
