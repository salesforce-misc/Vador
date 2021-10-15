package org.revcloud.vader.runner

import com.force.swag.id.ID
import com.force.swag.id.IdTraits
import de.cronn.reflection.util.PropertyUtils
import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function2
import io.vavr.Tuple2
import io.vavr.control.Either
import org.revcloud.vader.runner.IDConfig.IDConfigBuilder
import org.revcloud.vader.specs.component1
import org.revcloud.vader.specs.component2
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
private fun <IDT, ValidatableT, FailureT, EntityInfoT> idConfigToValidatorEtrs(
  config: IDConfig<IDT, ValidatableT, FailureT, EntityInfoT>?,
  idValidatorFallback: (IDT?) -> Boolean,
  optionalIdValidatorFallBack: (IDT?) -> Boolean
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
      optionalIdValidatorFallBack,
      false
    ) +
    toValidators12(
      config?.absentOrHaveValidSFIdFormatForAllOrFailWithFn,
      config?.withIdValidator,
      optionalIdValidatorFallBack,
      false
    ) +
    toValidators13(
      config?.absentOrHaveValidSFIdFormatOrFailWithFn,
      config?.withIdValidator,
      optionalIdValidatorFallBack,
      false
    )

@JvmSynthetic
private fun <IDT, ValidatableT, FailureT, EntityInfoT> toValidators11(
  config: Map<Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityInfoT>, FailureT?>?,
  idValidator: Function2<IDT, EntityInfoT, Boolean>?,
  idValidatorFallback: (IDT?) -> Boolean,
  optionalId: Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.map { (tuple2, failure) ->
    val (idFieldMapper, entityInfo) = tuple2
    ValidatorEtr { validatable ->
      validatable.map(idFieldMapper::get)
        .filterOrElse(
          validateId(idValidator, entityInfo, idValidatorFallback, optionalId)
        ) { failure }
    }
  } ?: emptyList()

@JvmSynthetic
private fun <IDT, ValidatableT, FailureT, EntityInfoT> toValidators12(
  config: Tuple2<Map<TypedPropertyGetter<ValidatableT, IDT?>, EntityInfoT>, Function2<String, IDT?, FailureT?>>?,
  idValidator: Function2<IDT, EntityInfoT, Boolean>?,
  idValidatorFallback: (IDT?) -> Boolean,
  optionalId: Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.let { (idFieldMapperToEntityInfo, failureFn) ->
    idFieldMapperToEntityInfo.map { (idFieldMapper, entityInfo) ->
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
private fun <IDT, ValidatableT, FailureT, EntityInfoT> toValidators13(
  config: Map<Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityInfoT>, Function2<String, IDT?, FailureT?>>?,
  idValidator: Function2<IDT, EntityInfoT, Boolean>?,
  idValidatorFallback: (IDT?) -> Boolean,
  optionalId: Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.map { (tuple2, failureFn) ->
    val (idFieldMapper, entityInfo) = tuple2
    ValidatorEtr { validatable ->
      validatable.map(idFieldMapper::get)
        .filterOrElse(
          validateId(idValidator, entityInfo, idValidatorFallback, optionalId),
          applyFailureFn(failureFn, validatable, idFieldMapper)
        )
    }
  } ?: emptyList()

@JvmSynthetic
private fun <IDT, EntityInfoT> validateId(
  idValidator: Function2<IDT, EntityInfoT, Boolean>?,
  entityInfo: EntityInfoT,
  idValidatorFallback: (IDT?) -> Boolean,
  optionalId: Boolean
) = { id: IDT? ->
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
    toValidatorEtrs4(config.withIdConfigs) +
    config.specs.map { it.toValidator() } +
    config.getValidators()
  )

private fun <FailureT, ValidatableT> toValidatorEtrs4(configs: Collection<IDConfigBuilder<*, ValidatableT, FailureT, *>>?): List<ValidatorEtr<ValidatableT, FailureT>> =
  configs?.flatMap {
    idConfigToValidatorEtrs(
      it.prepare(),
      fallBackValidator(isSFIdPresentAndValidFormat),
      fallBackValidator(isSFIdAbsentOrValidFormat)
    )
  } ?: emptyList()

private fun <ValidatableT, FailureT, FieldT> applyFailureFn(
  failureFn: Function2<String, FieldT, FailureT>?,
  validatable: Either<FailureT?, ValidatableT?>,
  fieldMapper: TypedPropertyGetter<in ValidatableT, out FieldT>
): (FieldT) -> FailureT? = { fieldValue: FieldT ->
  failureFn?.apply(getFieldName(validatable, fieldMapper), fieldValue)
}

private fun <ValidatableT, FailureT, FieldT> getFieldName(
  validatable: Either<FailureT?, ValidatableT?>,
  fieldMapper: TypedPropertyGetter<in ValidatableT, out FieldT>
): String = validatable.map { PropertyUtils.getPropertyName(validatable.get(), fieldMapper) }.getOrElse("Validatable is on Left")

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

private val fallBackValidator: ((ID?) -> Boolean) -> ((Any?) -> Boolean) = { fallBackValidator ->
  {
    when (it) {
      is String -> fallBackValidator(ID(it))
      is ID -> fallBackValidator(it)
      else -> throw IllegalArgumentException("Unknown Data-type for required ID: ${it?.javaClass?.name}")
    }
  }
}
