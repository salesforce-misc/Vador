/**
 * ****************************************************************************
 * Copyright (c) 2022, salesforce.com, inc. All rights reserved. SPDX-License-Identifier:
 * BSD-3-Clause For full license text, see the LICENSE file in the repo root or
 * https://opensource.org/licenses/BSD-3-Clause
 * ****************************************************************************
 */
package com.salesforce.vador.execution.strategies.util

import com.salesforce.vador.config.FieldConfig
import com.salesforce.vador.config.FieldConfig.FieldConfigBuilder
import com.salesforce.vador.config.IDConfig
import com.salesforce.vador.config.IDConfig.IDConfigBuilder
import com.salesforce.vador.config.base.BaseValidationConfig
import com.salesforce.vador.specs.component1
import com.salesforce.vador.specs.component2
import com.salesforce.vador.specs.specs.base.BaseSpec
import com.salesforce.vador.types.ValidatorEtr
import de.cronn.reflection.util.PropertyUtils
import de.cronn.reflection.util.TypedPropertyGetter
import io.vavr.Function2
import io.vavr.Tuple2
import io.vavr.control.Either
import java.util.Optional
import java.util.function.Predicate

// ! TODO 16/04/22: Break this file into smaller files.
@JvmSynthetic
internal fun <ValidatableT, FailureT> configToValidators(
  config: BaseValidationConfig<ValidatableT, FailureT>
): List<ValidatorEtr<ValidatableT?, FailureT?>> =
  (toValidatorEtrs1(config.shouldHaveFieldsOrFailWith, isFieldPresent) +
    toValidatorEtrs2(config.shouldHaveFieldsOrFailWithFn, isFieldPresent) +
    toValidatorEtrs3(config.shouldHaveFieldOrFailWithFn, isFieldPresent) +
    toValidatorEtrs4(config.withIdConfigs) +
    toValidatorEtrs5(config.withFieldConfigs) +
    config.specs.map { it.toValidator() } +
    config.toValidatorsEtr())

// * NOTE gopala.akshintala 14/08/22: Using Extension functions coz, names are almost same for all
// these
@JvmSynthetic
private fun <ValidatableT, FailureT> BaseValidationConfig<ValidatableT, FailureT>.toValidatorsEtr():
  List<ValidatorEtr<ValidatableT?, FailureT?>> =
  fromValidators1(withValidators) + fromValidators2(withValidator) + withValidatorEtrs

@JvmSynthetic
private fun <ValidatableT, FailureT, FieldT> toValidatorEtrs1(
  fieldMapperToFailure: Map<out TypedPropertyGetter<in ValidatableT, out FieldT>, FailureT>,
  fieldValidator: (FieldT) -> Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  fieldMapperToFailure.entries.map { (fieldMapper, failure) ->
    ValidatorEtr { it.map(fieldMapper::get).filterOrElse(fieldValidator) { failure } }
  }

@JvmSynthetic
private fun <ValidatableT, FailureT, FieldT> toValidatorEtrs2(
  fieldMappersToFailure:
    Tuple2<
      out Collection<TypedPropertyGetter<in ValidatableT, out FieldT>>,
      Function2<String, FieldT, FailureT>
    >?,
  fieldValidator: (FieldT) -> Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  fieldMappersToFailure?.let { (fieldMappers, failureFn) ->
    fieldMappers.map { fieldMapper ->
      ValidatorEtr { validatable ->
        validatable
          .map(fieldMapper::get)
          .filterOrElse(fieldValidator, applyFailureFn(failureFn, validatable, fieldMapper))
      }
    }
  }
    ?: emptyList()

@JvmSynthetic
private fun <ValidatableT, FailureT, FieldT> toValidatorEtrs3(
  fieldMapperToFailure:
    Map<out TypedPropertyGetter<in ValidatableT, out FieldT>, Function2<String, FieldT, FailureT>?>,
  fieldValidator: (FieldT) -> Boolean
): List<ValidatorEtr<ValidatableT, FailureT>> =
  fieldMapperToFailure.entries.map { (fieldMapper, failureFn) ->
    ValidatorEtr { validatable ->
      validatable
        .map(fieldMapper::get)
        .filterOrElse(fieldValidator, applyFailureFn(failureFn, validatable, fieldMapper))
    }
  }

@JvmSynthetic
private fun <IDT, ValidatableT, FailureT, EntityInfoT> idConfigToValidatorEtrs(
  config: IDConfig<IDT, ValidatableT, FailureT, EntityInfoT>?
): List<ValidatorEtr<ValidatableT, FailureT>> =
  toValidators11(config?.shouldHaveValidSFIdFormatForAllOrFailWith, config?.withIdValidator, true) +
    toValidators111(
      config?.shouldHaveValidSFPolymorphicIdFormatForAllOrFailWith,
      config?.withIdValidator,
      true
    ) +
    toValidators12(
      config?.shouldHaveValidSFIdFormatForAllOrFailWithFn,
      config?.withIdValidator,
      true
    ) +
    toValidators121(
      config?.shouldHaveValidSFPolymorphicIdFormatForAllOrFailWithFn,
      config?.withIdValidator,
      true
    ) +
    toValidators13(config?.shouldHaveValidSFIdFormatOrFailWithFn, config?.withIdValidator, true) +
    toValidators131(
      config?.shouldHaveValidSFPolymorphicIdFormatOrFailWithFn,
      config?.withIdValidator,
      true
    ) +
    toValidators11(config?.absentOrHaveValidSFIdFormatForAllOrFailWith, config?.withIdValidator) +
    toValidators11(config?.absentOrHaveValidSFIdFormatForAllOrFailWith, config?.withIdValidator) +
    toValidators12(config?.absentOrHaveValidSFIdFormatForAllOrFailWithFn, config?.withIdValidator) +
    toValidators121(
      config?.absentOrHaveValidSFPolymorphicIdFormatForAllOrFailWithFn,
      config?.withIdValidator
    ) +
    toValidators13(config?.absentOrHaveValidSFIdFormatOrFailWithFn, config?.withIdValidator) +
    toValidators131(
      config?.absentOrHaveValidSFPolymorphicIdFormatOrFailWithFn,
      config?.withIdValidator
    )

@JvmSynthetic
private fun <IDT, ValidatableT, FailureT, EntityInfoT> toValidators11(
  config: Map<Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityInfoT>, FailureT?>?,
  idValidator: Function2<IDT, EntityInfoT, Boolean>?,
  optionalId: Boolean = false
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.map { (tuple2, failure) ->
    val (idFieldMapper, entityInfo) = tuple2
    ValidatorEtr { validatable ->
      validatable.map(idFieldMapper::get).filterOrElse(
        validateId(idValidator, entityInfo, optionalId)
      ) {
        failure
      }
    }
  }
    ?: emptyList()

@JvmSynthetic
private fun <IDT, ValidatableT, FailureT, EntityInfoT> toValidators111(
  config:
    Map<Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<EntityInfoT>>, FailureT?>?,
  idValidator: Function2<IDT, EntityInfoT, Boolean>?,
  optionalId: Boolean = false
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.map { (tuple2, failure) ->
    val (idFieldMapper, entitiesInfo) = tuple2
    ValidatorEtr { validatable ->
      validatable.map(idFieldMapper::get).filterOrElse(
        validateId(idValidator, entitiesInfo, optionalId)
      ) {
        failure
      }
    }
  }
    ?: emptyList()

@JvmSynthetic
private fun <IDT, ValidatableT, FailureT, EntityInfoT> toValidators12(
  config:
    Tuple2<
      Map<TypedPropertyGetter<ValidatableT, IDT?>, EntityInfoT>, Function2<String, IDT?, FailureT?>
    >?,
  idValidator: Function2<IDT, EntityInfoT, Boolean>?,
  optionalId: Boolean = false
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.let { (idFieldMapperToEntityInfo, failureFn) ->
    idFieldMapperToEntityInfo.map { (idFieldMapper, entityInfo) ->
      ValidatorEtr { validatable ->
        validatable
          .map(idFieldMapper::get)
          .filterOrElse(
            validateId(idValidator, entityInfo, optionalId),
            applyFailureFn(failureFn, validatable, idFieldMapper)
          )
      }
    }
  }
    ?: emptyList()

@JvmSynthetic
private fun <IDT, ValidatableT, FailureT, EntityInfoT> toValidators121(
  config:
    Tuple2<
      Map<TypedPropertyGetter<ValidatableT, IDT?>, Collection<EntityInfoT>>,
      Function2<String, IDT?, FailureT?>
    >?,
  idValidator: Function2<IDT, EntityInfoT, Boolean>?,
  optionalId: Boolean = false
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.let { (idFieldMapperToEntityInfo, failureFn) ->
    idFieldMapperToEntityInfo.map { (idFieldMapper, entitiesInfo) ->
      ValidatorEtr { validatable ->
        validatable
          .map(idFieldMapper::get)
          .filterOrElse(
            validateId(idValidator, entitiesInfo, optionalId),
            applyFailureFn(failureFn, validatable, idFieldMapper)
          )
      }
    }
  }
    ?: emptyList()

@JvmSynthetic
private fun <IDT, ValidatableT, FailureT, EntityInfoT> toValidators13(
  config:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out EntityInfoT>,
      Function2<String, IDT?, FailureT?>
    >?,
  idValidator: Function2<IDT, EntityInfoT, Boolean>?,
  optionalId: Boolean = false
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.map { (tuple2, failureFn) ->
    val (idFieldMapper, entityInfo) = tuple2
    ValidatorEtr { validatable ->
      validatable
        .map(idFieldMapper::get)
        .filterOrElse(
          validateId(idValidator, entityInfo, optionalId),
          applyFailureFn(failureFn, validatable, idFieldMapper)
        )
    }
  }
    ?: emptyList()

@JvmSynthetic
private fun <IDT, ValidatableT, FailureT, EntityInfoT> toValidators131(
  config:
    Map<
      Tuple2<TypedPropertyGetter<ValidatableT, IDT?>, out Collection<EntityInfoT>>,
      Function2<String, IDT?, FailureT?>
    >?,
  idValidator: Function2<IDT, EntityInfoT, Boolean>?,
  optionalId: Boolean = false
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.map { (tuple2, failureFn) ->
    val (idFieldMapper, entitiesInfo) = tuple2
    ValidatorEtr { validatable ->
      validatable
        .map(idFieldMapper::get)
        .filterOrElse(
          validateId(idValidator, entitiesInfo, optionalId),
          applyFailureFn(failureFn, validatable, idFieldMapper)
        )
    }
  }
    ?: emptyList()

@JvmSynthetic
private fun <IDT, EntityInfoT> validateId(
  idValidator: Function2<IDT, EntityInfoT, Boolean>?,
  entityInfo: EntityInfoT,
  optionalId: Boolean
): (IDT?) -> Boolean = { id: IDT? ->
  when {
    idValidator != null ->
      when {
        optionalId -> id != null && idValidator.apply(id, entityInfo)
        else -> id == null || idValidator.apply(id, entityInfo)
      }
    else -> true
  }
}

@JvmSynthetic
private fun <IDT, EntityInfoT> validateId(
  idValidator: Function2<IDT, EntityInfoT, Boolean>?,
  entitiesInfo: Collection<EntityInfoT>,
  optionalId: Boolean
): (IDT?) -> Boolean = { id: IDT? ->
  when {
    idValidator != null ->
      when {
        optionalId -> id != null && entitiesInfo.any { idValidator.apply(id, it) }
        else -> id == null || entitiesInfo.any { idValidator.apply(id, it) }
      }
    else -> true
  }
}

@JvmSynthetic
private fun <FieldT> validateField(
  fieldValidator: Predicate<FieldT>?,
  optionalId: Boolean
): (FieldT?) -> Boolean = { id: FieldT? ->
  when {
    optionalId -> id != null && fieldValidator?.test(id) ?: true
    else -> id == null || fieldValidator?.test(id) ?: true
  }
}

@JvmSynthetic
private fun <FieldT, ValidatableT, FailureT> fieldConfigToValidatorEtrs(
  config: FieldConfig<FieldT, ValidatableT, FailureT>?
): List<ValidatorEtr<ValidatableT, FailureT>> =
  toFieldValidatorEtrs1(
    config?.shouldHaveValidFormatForAllOrFailWith,
    config?.withFieldValidator,
    true
  ) +
    toFieldValidatorEtrs2(
      config?.shouldHaveValidFormatForAllOrFailWithFn,
      config?.withFieldValidator,
      true
    ) +
    toFieldValidatorEtrs3(
      config?.shouldHaveValidFormatOrFailWithFn,
      config?.withFieldValidator,
      true
    ) +
    toFieldValidatorEtrs1(
      config?.absentOrHaveValidFormatForAllOrFailWith,
      config?.withFieldValidator
    ) +
    toFieldValidatorEtrs2(
      config?.absentOrHaveValidFormatForAllOrFailWithFn,
      config?.withFieldValidator
    ) +
    toFieldValidatorEtrs3(config?.absentOrHaveValidFormatOrFailWithFn, config?.withFieldValidator)

@JvmSynthetic
private fun <ValidatableT, FailureT, FieldT> toFieldValidatorEtrs1(
  fieldMapperToFailure: Map<out TypedPropertyGetter<in ValidatableT, out FieldT>, FailureT?>?,
  fieldValidator: Predicate<FieldT>?,
  optionalField: Boolean = false
): List<ValidatorEtr<ValidatableT, FailureT>> =
  fieldMapperToFailure?.entries?.map { (fieldMapper, failure) ->
    ValidatorEtr {
      it.map(fieldMapper::get).filterOrElse(validateField(fieldValidator, optionalField)) {
        failure
      }
    }
  }
    ?: emptyList()

@JvmSynthetic
private fun <ValidatableT, FailureT, FieldT> toFieldValidatorEtrs2(
  config:
    Tuple2<
      Collection<TypedPropertyGetter<ValidatableT, FieldT>>, Function2<String, FieldT?, FailureT?>
    >?,
  fieldValidator: Predicate<FieldT>?,
  optionalField: Boolean = false
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.let { (fieldMappers, failureFn) ->
    fieldMappers.map { fieldMapper ->
      ValidatorEtr { validatable ->
        validatable
          .map(fieldMapper::get)
          .filterOrElse(
            validateField(fieldValidator, optionalField),
            applyFailureFn(failureFn, validatable, fieldMapper)
          )
      }
    }
  }
    ?: emptyList()

@JvmSynthetic
private fun <FieldT, ValidatableT, FailureT> toFieldValidatorEtrs3(
  config: Map<TypedPropertyGetter<ValidatableT, FieldT>, Function2<String, FieldT?, FailureT?>>?,
  fieldValidator: Predicate<FieldT>?,
  optionalField: Boolean = false
): List<ValidatorEtr<ValidatableT, FailureT>> =
  config?.map { (fieldMapper, failureFn) ->
    ValidatorEtr { validatable ->
      validatable
        .map(fieldMapper::get)
        .filterOrElse(
          validateField(fieldValidator, optionalField),
          applyFailureFn(failureFn, validatable, fieldMapper)
        )
    }
  }
    ?: emptyList()

private fun <FailureT, ValidatableT> toValidatorEtrs4(
  configs: Collection<IDConfigBuilder<*, ValidatableT, FailureT, *>>?
): List<ValidatorEtr<ValidatableT, FailureT>> =
  configs?.flatMap { idConfigToValidatorEtrs(it.prepare()) } ?: emptyList()

private fun <FailureT, ValidatableT> toValidatorEtrs5(
  configs: Collection<FieldConfigBuilder<*, ValidatableT, FailureT>>?
): List<ValidatorEtr<ValidatableT, FailureT>> =
  configs?.flatMap { fieldConfigToValidatorEtrs(it.prepare()) } ?: emptyList()

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
): String =
  validatable
    .map { PropertyUtils.getPropertyName(validatable.get(), fieldMapper) }
    .getOrElse("Validatable is on Left")

@JvmSynthetic
private fun <ValidatableT, FailureT> BaseSpec<ValidatableT, FailureT>.toValidator():
  ValidatorEtr<ValidatableT?, FailureT?> = ValidatorEtr {
  it.filterOrElse(toPredicate()) { validatable -> getFailure(validatable) }
}

@JvmSynthetic
internal val isFieldPresent: (Any?) -> Boolean = {
  when (it) {
    null -> false
    is String -> it.isNotBlank()
    is List<*> -> it.isNotEmpty()
    is Optional<*> -> it.isPresent
    else -> true
  }
}
