/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.lift;

import static com.salesforce.vador.lift.InheritanceLiftEtrUtil.liftAllToChildValidatorType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static sample.consumer.failure.ValidationFailure.NONE;
import static sample.consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;

import com.salesforce.vador.config.ValidationConfig;
import com.salesforce.vador.execution.Vador;
import com.salesforce.vador.types.Validator;
import com.salesforce.vador.types.ValidatorEtr;
import io.vavr.Tuple;
import io.vavr.control.Either;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sample.consumer.bean.Container;
import sample.consumer.bean.Member;
import sample.consumer.config.ConfigForValidators;
import sample.consumer.failure.ValidationFailure;
import sample.consumer.failure.ValidationFailureMessage;

class InheritanceLiftEtrUtilKtTest {

	@Test
	void liftParentToChildValidatorTypeTest() {
		final Validator<Parent, ValidationFailure> v1 = ignore -> NONE;
		final Validator<Child, ValidationFailure> v2 = ignore -> UNKNOWN_EXCEPTION;
		final var validationConfig =
				ValidationConfig.<Child, ValidationFailure>toValidate()
						.withValidators(Tuple.of(List.of(v1, v2), NONE))
						.prepare();
		final var result = Vador.validateAndFailFast(new Child(), validationConfig);
		assertThat(result).contains(UNKNOWN_EXCEPTION);
	}

	@Test
	void liftParentToChildValidatorEtrTypeTest() {
		final ValidatorEtr<Parent, ValidationFailure> v1 = ignore -> Either.right(NONE);
		final ValidatorEtr<Parent, ValidationFailure> v2 = ignore -> Either.right(NONE);
		final ValidatorEtr<Child, ValidationFailure> v3 = ignore -> Either.left(UNKNOWN_EXCEPTION);
		final var validationConfig =
				ValidationConfig.<Child, ValidationFailure>toValidate()
						.withValidatorEtrs(liftAllToChildValidatorType(List.of(v1, v2)))
						.withValidatorEtr(v3)
						.prepare();
		final var result = Vador.validateAndFailFast(new Child(), validationConfig);
		assertThat(result).contains(UNKNOWN_EXCEPTION);
	}

	@Test
	void javaConsumerValuesRetainTheirConstructorsGettersAndValueContract() {
		final var member = new Member(7);
		final var parent =
				new sample.consumer.bean.Parent(
						1, "sf-id", member, 2, "required-2", "required-3", "sf-id-1", "sf-id-2");
		final var sameParent =
				new sample.consumer.bean.Parent(
						1, "sf-id", new Member(7), 2, "required-2", "required-3", "sf-id-1", "sf-id-2");
		final var requiredOnlyParent = new sample.consumer.bean.Parent(1, "sf-id", member);

		assertThat(parent.getId()).isEqualTo(1);
		assertThat(parent.getSfId()).isEqualTo("sf-id");
		assertThat(parent.getMember()).isEqualTo(member);
		assertThat(parent.getRequiredField1()).isEqualTo(2);
		assertThat(parent.getRequiredField2()).isEqualTo("required-2");
		assertThat(parent.getRequiredField3()).isEqualTo("required-3");
		assertThat(parent.getSfId1()).isEqualTo("sf-id-1");
		assertThat(parent.getSfId2()).isEqualTo("sf-id-2");
		assertThat(parent).isEqualTo(sameParent).hasSameHashCodeAs(sameParent);
		assertThat(parent.toString()).contains("sf-id", "required-3");
		assertThat(requiredOnlyParent.getRequiredField1()).isNull();
		assertThat(requiredOnlyParent.getRequiredField2()).isNull();
		assertThat(requiredOnlyParent.getRequiredField3()).isNull();
		assertThat(requiredOnlyParent.getSfId1()).isNull();
		assertThat(requiredOnlyParent.getSfId2()).isNull();
		assertThat(member).isEqualTo(new Member(7)).hasSameHashCodeAs(new Member(7));
		assertThat(member).isNotEqualTo(new Member(8));
		assertThat(member.getId()).isEqualTo(7);
		assertThat(member.toString()).contains("7");

		final var container = new Container(3, member);
		final var equivalentParent = new sample.consumer.bean.Parent(3, null, new Member(7));
		assertThat(container.getMember()).isSameAs(member);
		assertThat(new Container(3).getId()).isEqualTo(3);
		assertThat(new Container("container-sf-id").getSfId()).isEqualTo("container-sf-id");
		assertThat(container.toString()).contains("Parent", "3", "7");
		assertThat(equivalentParent).isEqualTo(container).hasSameHashCodeAs(container);
		assertThat(container).isEqualTo(equivalentParent).hasSameHashCodeAs(equivalentParent);
	}

	@ParameterizedTest
	@MethodSource("parentsWithOneDifferentField")
	void parentValueEqualityCoversEveryField(sample.consumer.bean.Parent differentParent) {
		final var parent =
				new sample.consumer.bean.Parent(
						1, "sf-id", new Member(7), 2, "required-2", "required-3", "sf-id-1", "sf-id-2");

		assertThat(parent).isNotEqualTo(differentParent);
		assertThat(differentParent).isNotEqualTo(parent);
	}

	@Test
	void mutableValidationFailuresRetainReferenceAndValueSemantics() {
		final var sampleMessage = ValidationFailureMessage.MSG_WITH_PARAMS;
		final var sampleParams = new Object[] {"before"};
		final var sampleFailure = sampleFailure(sampleMessage, "exception");
		final var sameSampleFailure = sampleFailure(sampleMessage, "exception");
		final var sampleFailureWithDifferentMessage =
				sampleFailure(ValidationFailureMessage.INVALID_VALUE, "exception");
		final var sampleFailureWithDifferentException = sampleFailure(sampleMessage, "different");

		sampleMessage.setParams(sampleParams);
		final var sampleHashBeforeArrayMutation = sampleFailure.hashCode();
		sampleParams[0] = "after";

		assertThat(sampleMessage.getParams()).isSameAs(sampleParams).containsExactly("after");
		assertThat(sampleFailure.getValidationFailureMessage()).isSameAs(sampleMessage);
		assertThat(sampleFailure.getExceptionMsg()).isEqualTo("exception");
		assertThat(sampleFailure).isEqualTo(sameSampleFailure).hasSameHashCodeAs(sameSampleFailure);
		assertThat(sampleFailure).isNotEqualTo(sampleFailureWithDifferentMessage);
		assertThat(sampleFailure).isNotEqualTo(sampleFailureWithDifferentException);
		assertThat(sampleFailure.hashCode()).isEqualTo(sampleHashBeforeArrayMutation);
		assertThat(sampleFailure.toString()).contains("MSG_WITH_PARAMS", "exception");
		sampleMessage.setParams(null);
		assertThat(sampleMessage.getParams()).isNull();

		final var specMessage =
				com.salesforce.vador.specs.failure.ValidationFailureMessage.INVALID_VALUE;
		final var specParams = new Object[] {"before"};
		final var specFailure = specFailure(specMessage, "exception");
		final var sameSpecFailure = specFailure(specMessage, "exception");
		final var specFailureWithDifferentMessage =
				specFailure(com.salesforce.vador.specs.failure.ValidationFailureMessage.NONE, "exception");
		final var specFailureWithDifferentException = specFailure(specMessage, "different");

		specMessage.setParams(specParams);
		final var specHashBeforeArrayMutation = specFailure.hashCode();
		specParams[0] = "after";

		assertThat(specMessage.getSection()).isEmpty();
		assertThat(specMessage.getName()).isEqualTo("InvalidValue");
		assertThat(specMessage.getParams()).isSameAs(specParams).containsExactly("after");
		assertThat(specFailure.getValidationFailureMessage()).isSameAs(specMessage);
		assertThat(specFailure.getExceptionMsg()).isEqualTo("exception");
		assertThat(specFailure).isEqualTo(sameSpecFailure).hasSameHashCodeAs(sameSpecFailure);
		assertThat(specFailure).isNotEqualTo(specFailureWithDifferentMessage);
		assertThat(specFailure).isNotEqualTo(specFailureWithDifferentException);
		assertThat(specFailure.hashCode()).isEqualTo(specHashBeforeArrayMutation);
		assertThat(specFailure.toString()).contains("INVALID_VALUE", "exception");
		specMessage.setParams(null);
		assertThat(specMessage.getParams()).isNull();
	}

	@Test
	void validatorBeanRetainsItsGeneratedValueContract() throws ReflectiveOperationException {
		final var beanClass = Class.forName("sample.consumer.validators.BeanValidator$Bean");
		final var constructor = beanClass.getDeclaredConstructor(String.class);
		final var getId = beanClass.getDeclaredMethod("getId");
		constructor.setAccessible(true);
		getId.setAccessible(true);
		final var bean = constructor.newInstance("id");
		final var sameBean = constructor.newInstance("id");
		final var beanWithDifferentId = constructor.newInstance("different-id");

		assertThat(Modifier.isPrivate(beanClass.getModifiers())).isTrue();
		assertThat(Modifier.isStatic(beanClass.getModifiers())).isTrue();
		assertThat(Modifier.isFinal(beanClass.getModifiers())).isTrue();
		assertThat(Modifier.isPublic(constructor.getModifiers())).isTrue();
		assertThat(getId.invoke(bean)).isEqualTo("id");
		assertThat(bean).isEqualTo(sameBean).hasSameHashCodeAs(sameBean);
		assertThat(bean).isNotEqualTo(beanWithDifferentId);
	}

	@Test
	void configForValidatorsRemainsAStatelessUtility() throws ReflectiveOperationException {
		final var constructor = ConfigForValidators.class.getDeclaredConstructor();

		assertThat(Modifier.isFinal(ConfigForValidators.class.getModifiers())).isTrue();
		assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
		assertThat(ConfigForValidators.getServiceValidations()).hasSize(2);
		assertThat(ConfigForValidators.getParentValidations()).isNull();
		assertThat(ConfigForValidators.getParentSimpleValidations()).hasSize(2);
		assertThat(ConfigForValidators.getSimpleServiceValidations()).hasSize(2);

		constructor.setAccessible(true);
		assertThat(catchThrowable(constructor::newInstance))
				.isInstanceOf(InvocationTargetException.class)
				.hasCauseInstanceOf(AssertionError.class);
	}

	@Test
	void childEqualityRetainsItsSuperclassContract() {
		final var child = new Child();

		assertThat(child).isEqualTo(child);
		assertThat(child).isNotEqualTo(new Child());
		assertThat(child.toString()).contains("Child");
	}

	private static Stream<sample.consumer.bean.Parent> parentsWithOneDifferentField() {
		return Stream.of(
				new sample.consumer.bean.Parent(
						2, "sf-id", new Member(7), 2, "required-2", "required-3", "sf-id-1", "sf-id-2"),
				new sample.consumer.bean.Parent(
						1,
						"different-sf-id",
						new Member(7),
						2,
						"required-2",
						"required-3",
						"sf-id-1",
						"sf-id-2"),
				new sample.consumer.bean.Parent(
						1, "sf-id", new Member(8), 2, "required-2", "required-3", "sf-id-1", "sf-id-2"),
				new sample.consumer.bean.Parent(
						1, "sf-id", new Member(7), 3, "required-2", "required-3", "sf-id-1", "sf-id-2"),
				new sample.consumer.bean.Parent(
						1,
						"sf-id",
						new Member(7),
						2,
						"different-required-2",
						"required-3",
						"sf-id-1",
						"sf-id-2"),
				new sample.consumer.bean.Parent(
						1,
						"sf-id",
						new Member(7),
						2,
						"required-2",
						"different-required-3",
						"sf-id-1",
						"sf-id-2"),
				new sample.consumer.bean.Parent(
						1,
						"sf-id",
						new Member(7),
						2,
						"required-2",
						"required-3",
						"different-sf-id-1",
						"sf-id-2"),
				new sample.consumer.bean.Parent(
						1,
						"sf-id",
						new Member(7),
						2,
						"required-2",
						"required-3",
						"sf-id-1",
						"different-sf-id-2"));
	}

	private static ValidationFailure sampleFailure(
			ValidationFailureMessage validationFailureMessage, String exceptionMsg) {
		final var validationFailure = new ValidationFailure(validationFailureMessage);
		validationFailure.setExceptionMsg(exceptionMsg);
		return validationFailure;
	}

	private static com.salesforce.vador.specs.failure.ValidationFailure specFailure(
			com.salesforce.vador.specs.failure.ValidationFailureMessage validationFailureMessage,
			String exceptionMsg) {
		final var validationFailure =
				new com.salesforce.vador.specs.failure.ValidationFailure(validationFailureMessage);
		validationFailure.setExceptionMsg(exceptionMsg);
		return validationFailure;
	}

	private abstract class Parent {}

	private class Child extends Parent {

		public Child() {}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (other == null || getClass() != other.getClass()) {
				return false;
			}
			return super.equals(other);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}

		@Override
		public String toString() {
			return "InheritanceLiftEtrUtilKtTest.Child()";
		}
	}
}
