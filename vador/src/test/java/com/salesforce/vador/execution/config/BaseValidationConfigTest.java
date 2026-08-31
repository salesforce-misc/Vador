/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.execution.config;

import static com.salesforce.vador.execution.Vador.validateAndFailFast;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static sample.consumer.failure.ValidationFailure.NONE;
import static sample.consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static sample.consumer.failure.ValidationFailure.REQUIRED_FIELD_MISSING;
import static sample.consumer.failure.ValidationFailure.REQUIRED_FIELD_MISSING_1;
import static sample.consumer.failure.ValidationFailure.REQUIRED_FIELD_MISSING_2;
import static sample.consumer.failure.ValidationFailure.REQUIRED_LIST_MISSING;
import static sample.consumer.failure.ValidationFailure.getFailureWithParams;

import com.salesforce.vador.config.FieldConfig;
import com.salesforce.vador.config.ValidationConfig;
import com.salesforce.vador.types.Specs;
import io.vavr.Tuple;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;
import sample.consumer.failure.ValidationFailureMessage;

/** Contains tests for all DSL common to configs derived from BaseValidationConfig */
class BaseValidationConfigTest {

	// tag::validationConfig-for-flat-bean-demo[]
	@DisplayName("Cases covered - Missing Field, String Field, List Field")
	@Test
	void failFastWithRequiredFieldsMissing() {
		final var validationConfig =
				ValidationConfig.<BaseValidationConfigBean, ValidationFailure>toValidate()
						.shouldHaveFieldsOrFailWith(
								Map.of(
										BaseValidationConfigBean::getRequiredField1, REQUIRED_FIELD_MISSING_1,
										BaseValidationConfigBean::getRequiredField2, REQUIRED_FIELD_MISSING_2,
										BaseValidationConfigBean::getRequiredList, REQUIRED_LIST_MISSING))
						.withValidatorEtr(
								beanEtr -> beanEtr.filterOrElse(Objects::nonNull, ignore -> NOTHING_TO_VALIDATE))
						.prepare();

		final var validatableWithBlankReqField =
				new BaseValidationConfigBean(0, "", null, null, List.of("1"));
		final var result1 = validateAndFailFast(validatableWithBlankReqField, validationConfig);
		assertThat(result1).contains(REQUIRED_FIELD_MISSING_2);

		final var validatableWithNullReqField =
				new BaseValidationConfigBean(null, "2", null, null, List.of("1"));
		final var result2 = validateAndFailFast(validatableWithNullReqField, validationConfig);
		assertThat(result2).contains(REQUIRED_FIELD_MISSING_1);

		final var validatableWithEmptyReqList =
				new BaseValidationConfigBean(1, "2", null, null, emptyList());
		final var result3 = validateAndFailFast(validatableWithEmptyReqList, validationConfig);
		assertThat(result3).contains(REQUIRED_LIST_MISSING);
	}

	// end::validationConfig-for-flat-bean-demo[]

	@Test
	void failFastWithRequiredFieldMissingFailWithFn() {
		final var validationConfig =
				ValidationConfig.<BaseValidationConfigBean, ValidationFailure>toValidate()
						.shouldHaveFieldsOrFailWithFn(
								Tuple.of(
										List.of(
												BaseValidationConfigBean::getRequiredField1,
												BaseValidationConfigBean::getRequiredField2,
												BaseValidationConfigBean::getRequiredList),
										(missingFieldName, missingFieldValue) ->
												getFailureWithParams(
														REQUIRED_FIELD_MISSING,
														missingFieldName,
														missingFieldValue + "missing")))
						.prepare();
		final var expectedFieldNames =
				Set.of(
						BaseValidationConfigBean.Fields.requiredField1,
						BaseValidationConfigBean.Fields.requiredField2,
						BaseValidationConfigBean.Fields.requiredList);
		assertThat(validationConfig.getRequiredFieldNames(BaseValidationConfigBean.class))
				.isEqualTo(expectedFieldNames);
		final var withRequiredFieldNull = new BaseValidationConfigBean(1, "", null, null, emptyList());

		final var result = validateAndFailFast(withRequiredFieldNull, validationConfig);
		assertThat(result).isPresent();
		assertThat(result.get().getValidationFailureMessage().getParams())
				.containsExactly(BaseValidationConfigBean.Fields.requiredField2, "missing");
	}

	@DisplayName("Cases covered - Optional field missing")
	@Test
	void failFastWithRequiredFieldMissingFailWithFn2() {
		final var validationConfig =
				ValidationConfig.<BaseValidationConfigBean1, ValidationFailure>toValidate()
						.shouldHaveFieldOrFailWithFn(
								BaseValidationConfigBean1::getStr,
								(fieldName, value) -> {
									assertThat(fieldName).isEqualTo(BaseValidationConfigBean1.Fields.str);
									return REQUIRED_FIELD_MISSING;
								})
						.prepare();
		var bean1 = new BaseValidationConfigBean1(Optional.empty());
		final var result = validateAndFailFast(bean1, validationConfig);
		assertThat(result).contains(REQUIRED_FIELD_MISSING);
	}

	@Test
	void getSpecWithNameWithDuplicateNames() {
		final var duplicateSpecName = "DuplicateSpecName";
		final var specsForConfig =
				(Specs<BaseValidationConfigBeanWithIdStrFields, ValidationFailure>)
						spec ->
								List.of(
										spec._1()
												.nameForTest(duplicateSpecName)
												.given(BaseValidationConfigBeanWithIdStrFields::getRequiredField),
										spec._1()
												.nameForTest(duplicateSpecName)
												.given(BaseValidationConfigBeanWithIdStrFields::getContactId));
		final var validationConfig =
				ValidationConfig.<BaseValidationConfigBeanWithIdStrFields, ValidationFailure>toValidate()
						.specify(specsForConfig)
						.prepare();
		Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> validationConfig.getPredicateOfSpecForTest(duplicateSpecName));
	}

	@Test
	void getFieldNames() {
		final var validationConfig =
				ValidationConfig.<BaseValidationConfigBeanWithIdStrFields, ValidationFailure>toValidate()
						.shouldHaveFieldOrFailWith(
								BaseValidationConfigBeanWithIdStrFields::getRequiredField, NONE)
						.prepare();
		assertThat(
						validationConfig.getRequiredFieldNames(BaseValidationConfigBeanWithIdStrFields.class))
				.contains(BaseValidationConfigBeanWithIdStrFields.Fields.requiredField);
	}

	@Test
	@DisplayName("Validator Types")
	void validatorTypes() {
		final var validationConfig1 =
				ValidationConfig.<BaseValidationConfigBean, ValidationFailure>toValidate()
						.withValidator(
								bean -> bean.getRequiredField1() == null ? REQUIRED_FIELD_MISSING_1 : NONE,
								REQUIRED_FIELD_MISSING_1)
						.prepare();
		assertThat(validationConfig1.getValidatableType()).isEqualTo(BaseValidationConfigBean.class);

		final var validationConfig2 =
				ValidationConfig.<BaseValidationConfigBean, ValidationFailure>toValidate()
						.shouldHaveFieldOrFailWithFn(
								BaseValidationConfigBean::getRequiredField1,
								(fieldName, value) -> {
									assertThat(fieldName).isEqualTo(BaseValidationConfigBean.Fields.requiredField1);
									return REQUIRED_FIELD_MISSING_1;
								})
						.prepare();
		assertThat(validationConfig2.getValidatableType()).isEqualTo(BaseValidationConfigBean.class);
	}

	// tag::validationConfig-for-nested-bean-demo[]
	@Test
	void nestedBeanValidationWithInvalidMember() {
		final var memberValidationConfig =
				ValidationConfig.<BaseValidationConfigBean, ValidationFailure>toValidate()
						.withFieldConfig(
								FieldConfig.<String, BaseValidationConfigBean, ValidationFailure>toValidate()
										.withFieldValidator(fieldStr -> !"invalidSfId".equals(fieldStr))
										.shouldHaveValidFormatOrFailWithFn(
												BaseValidationConfigBean::getSfId2,
												(name, value) ->
														getFailureWithParams(
																ValidationFailureMessage.MSG_WITH_PARAMS, name, value)))
						.prepare();
		final var containerValidationConfig =
				ValidationConfig.<BaseValidationConfigContainerBean, ValidationFailure>toValidate()
						.shouldHaveFieldOrFailWithFn(
								BaseValidationConfigContainerBean::getRequiredField,
								(name, value) ->
										getFailureWithParams(ValidationFailureMessage.MSG_WITH_PARAMS, name, value))
						.prepare();

		final String invalidSfId = "invalidSfId";
		final var memberWithInvalidSfId =
				new BaseValidationConfigBean(null, null, "1ttxx00000000hZAAQ", invalidSfId, emptyList());
		final var validContainer =
				new BaseValidationConfigContainerBean("requiredField", memberWithInvalidSfId);
		final var result =
				validateAndFailFast(validContainer, containerValidationConfig)
						.or(() -> validateAndFailFast(memberWithInvalidSfId, memberValidationConfig));

		assertThat(result).isPresent();
		assertThat(result.get().getValidationFailureMessage().getParams())
				.containsExactly(BaseValidationConfigBean.Fields.sfId2, invalidSfId);
	}

	// end::validationConfig-for-nested-bean-demo[]

}
