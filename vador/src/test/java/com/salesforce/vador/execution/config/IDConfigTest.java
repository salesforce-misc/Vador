/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.execution.config;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static org.assertj.core.api.Assertions.assertThat;
import static sample.consumer.failure.ValidationFailure.INVALID_OPTIONAL_UDD_ID;
import static sample.consumer.failure.ValidationFailure.INVALID_POLYMORPHIC_UDD_ID;
import static sample.consumer.failure.ValidationFailure.INVALID_UDD_ID;
import static sample.consumer.failure.ValidationFailure.INVALID_UDD_ID_2;
import static sample.consumer.failure.ValidationFailure.INVALID_UDD_ID_3;
import static sample.consumer.failure.ValidationFailure.getFailureWithParams;

import com.salesforce.vador.config.BatchValidationConfig;
import com.salesforce.vador.config.IDConfig;
import com.salesforce.vador.config.ValidationConfig;
import com.salesforce.vador.execution.Vador;
import com.salesforce.vador.execution.VadorBatch;
import io.vavr.Tuple;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;

class IDConfigTest {
	private static final String ACCOUNT_ID = "accountId";
	private static final String CONTACT_ID = "contactId";
	private static final String PRODUCT_ID = "productId";
	private static final String VALID_ACCOUNT_ID = "validAccountId";
	private static final String VALID_CONTACT_ID = "validContactId";

	private static final String INVALID_SF_ID = "invalidSFId";
	private static final String INVALID_SF_POLYMORPHIC_ID = "invalidSFPolymorphicId";
	private static final Map<String, Class<? extends EntityId>> ID_TO_ENTITY_ID =
			Map.of(
					VALID_ACCOUNT_ID, IDConfigAccountEntityId.class,
					VALID_CONTACT_ID, IDConfigContactEntityId.class);

	@Test
	void idConfigWithShouldHaveValidSFIdFormatForAllOrFailWithFn() {
		final var config =
				ValidationConfig.<IDConfigBeanWithIdFields2, ValidationFailure>toValidate()
						.withIdConfig(
								IDConfig
										.<IDConfigID, IDConfigBeanWithIdFields2, ValidationFailure, EntityId>
												toValidate()
										.withIdValidator(ValidIdUtil::isThisEntity)
										.shouldHaveValidSFIdFormatForAllOrFailWithFn(
												Tuple.of(
														Map.of(
																IDConfigBeanWithIdFields2::getAccountId,
																		AccountUddConstants.EntityId,
																IDConfigBeanWithIdFields2::getContactId,
																		ContactUddConstants.EntityId),
														(invalidIdFieldName, invalidIdFieldValue) ->
																getFailureWithParams(
																		INVALID_UDD_ID, invalidIdFieldName, invalidIdFieldValue))))
						.prepare();
		final var invalidContactId = new IDConfigID(INVALID_SF_ID);
		final var result =
				Vador.validateAndFailFast(
						new IDConfigBeanWithIdFields2(new IDConfigID(VALID_ACCOUNT_ID), invalidContactId),
						config);
		assertThat(result).isPresent().contains(INVALID_UDD_ID);
		assertThat(result.get().getValidationFailureMessage().getParams())
				.containsExactly(CONTACT_ID, invalidContactId);
	}

	@Test
	void idConfigWithShouldHaveValidSFIdFormatForAllOrFailWith() {
		final var config =
				ValidationConfig.<IDConfigBeanWithIdFields2, ValidationFailure>toValidate()
						.withIdConfig(
								IDConfig
										.<IDConfigID, IDConfigBeanWithIdFields2, ValidationFailure, EntityId>
												toValidate()
										.withIdValidator(ValidIdUtil::isThisEntity)
										.shouldHaveValidSFIdFormatForAllOrFailWith(
												Map.of(
														Tuple.of(
																IDConfigBeanWithIdFields2::getAccountId,
																AccountUddConstants.EntityId),
														getFailureWithParams(INVALID_UDD_ID, ACCOUNT_ID),
														Tuple.of(
																IDConfigBeanWithIdFields2::getContactId,
																ContactUddConstants.EntityId),
														getFailureWithParams(INVALID_UDD_ID_2, CONTACT_ID))))
						.prepare();
		final var invalidContactId = new IDConfigID(INVALID_SF_ID);

		final var result =
				Vador.validateAndFailFast(
						new IDConfigBeanWithIdFields2(new IDConfigID(VALID_ACCOUNT_ID), invalidContactId),
						config);
		assertThat(result).isPresent().contains(INVALID_UDD_ID_2);
		assertThat(result.get().getValidationFailureMessage().getParams()).containsExactly(CONTACT_ID);
	}

	@DisplayName(
			"IdConfig With `shouldHaveValidSFIdFormatForAllOrFailWith` And `AbsentOrHaveValidSFIdFormatOrFailWith`")
	@Test
	void idConfigWithMultipleConditions() {
		final var config =
				ValidationConfig.<IDConfigBeanWithIdFields3, ValidationFailure>toValidate()
						.withIdConfig(
								IDConfig
										.<IDConfigID, IDConfigBeanWithIdFields3, ValidationFailure, EntityId>
												toValidate()
										.withIdValidator(ValidIdUtil::isThisEntity)
										.shouldHaveValidSFIdFormatForAllOrFailWith(
												Map.of(
														Tuple.of(
																IDConfigBeanWithIdFields3::getAccountId,
																AccountUddConstants.EntityId),
														getFailureWithParams(INVALID_UDD_ID, ACCOUNT_ID),
														Tuple.of(
																IDConfigBeanWithIdFields3::getContactId,
																ContactUddConstants.EntityId),
														getFailureWithParams(INVALID_UDD_ID_2, CONTACT_ID)))
										.absentOrHaveValidSFIdFormatOrFailWith(
												Tuple.of(
														IDConfigBeanWithIdFields3::getProductId, ProductUddConstants.EntityId),
												getFailureWithParams(INVALID_UDD_ID_3, PRODUCT_ID)))
						.prepare();
		final var invalidProductId = new IDConfigID(INVALID_SF_ID);
		final var result =
				Vador.validateAndFailFast(
						new IDConfigBeanWithIdFields3(
								new IDConfigID(VALID_ACCOUNT_ID),
								new IDConfigID(VALID_CONTACT_ID),
								invalidProductId),
						config);
		assertThat(result).isPresent().contains(INVALID_UDD_ID_3);
		assertThat(result.get().getValidationFailureMessage().getParams()).containsExactly(PRODUCT_ID);
	}

	@Test
	void idConfigWithStrIds() {
		final var config =
				ValidationConfig.<BaseValidationConfigBeanWithIdStrFields, ValidationFailure>toValidate()
						.withIdConfig(
								IDConfig
										.<String, BaseValidationConfigBeanWithIdStrFields, ValidationFailure, EntityId>
												toValidate()
										.withIdValidator(ValidIdUtil::isThisEntity)
										.shouldHaveValidSFIdFormatForAllOrFailWithFn(
												Tuple.of(
														Map.of(
																BaseValidationConfigBeanWithIdStrFields::getAccountId,
																		AccountUddConstants.EntityId,
																BaseValidationConfigBeanWithIdStrFields::getContactId,
																		ContactUddConstants.EntityId),
														(invalidIdFieldName, invalidIdFieldValue) ->
																getFailureWithParams(
																		INVALID_UDD_ID, invalidIdFieldName, invalidIdFieldValue))))
						.prepare();
		final var result =
				Vador.validateAndFailFast(
						new BaseValidationConfigBeanWithIdStrFields(null, INVALID_SF_ID, null), config);
		assertThat(result).contains(INVALID_UDD_ID);
	}

	@Test
	void idConfigWithMixOfIdsAndStrIds() {
		final var config =
				ValidationConfig.<IDConfigBeanWithMixIdFields, ValidationFailure>toValidate()
						.withIdConfig(
								IDConfig
										.<IDConfigID, IDConfigBeanWithMixIdFields, ValidationFailure, EntityId>
												toValidate()
										.withIdValidator(ValidIdUtil::isThisEntity)
										.shouldHaveValidSFIdFormatOrFailWith(
												Tuple.of(
														IDConfigBeanWithMixIdFields::getAccountId,
														AccountUddConstants.EntityId),
												INVALID_UDD_ID))
						.withIdConfig(
								IDConfig
										.<String, IDConfigBeanWithMixIdFields, ValidationFailure, EntityId>toValidate()
										.withIdValidator(ValidIdUtil::isThisEntity)
										.absentOrHaveValidSFIdFormatOrFailWith(
												Tuple.of(
														IDConfigBeanWithMixIdFields::getContactId,
														ContactUddConstants.EntityId),
												INVALID_OPTIONAL_UDD_ID))
						.prepare();
		final var result =
				Vador.validateAndFailFast(
						new IDConfigBeanWithMixIdFields(null, new IDConfigID(INVALID_SF_ID), null), config);
		assertThat(result).contains(INVALID_UDD_ID);
	}

	@Test
	@DisplayName("Validator types with IdConfig")
	void validatorTypesWithIdConfig() {
		final var config =
				ValidationConfig.<IDConfigBeanWithMixIdFields, ValidationFailure>toValidate()
						.withIdConfig(
								IDConfig
										.<IDConfigID, IDConfigBeanWithMixIdFields, ValidationFailure, EntityId>
												toValidate()
										.withIdValidator(ValidIdUtil::isThisEntity)
										.shouldHaveValidSFIdFormatOrFailWith(
												Tuple.of(
														IDConfigBeanWithMixIdFields::getAccountId,
														AccountUddConstants.EntityId),
												INVALID_UDD_ID))
						.prepare();
		assertThat(config.getValidatableType()).isEqualTo(IDConfigBeanWithMixIdFields.class);
	}

	@Test
	void idConfigWithShouldHaveValidSFPolymorphicIdFormatForAllOrFailWith() {
		final var config =
				ValidationConfig.<IDConfigBeanWithPolymorphicIdFields, ValidationFailure>toValidate()
						.withIdConfig(
								IDConfig
										.<IDConfigID, IDConfigBeanWithPolymorphicIdFields, ValidationFailure, EntityId>
												toValidate()
										.withIdValidator(ValidIdUtil::isThisEntity)
										.shouldHaveValidSFPolymorphicIdFormatOrFailWith(
												Tuple.of(
														IDConfigBeanWithPolymorphicIdFields::getAccountOrContactId,
														PolymorphicUddFactory.DOMAIN_SET),
												INVALID_POLYMORPHIC_UDD_ID))
						.prepare();
		final var result =
				Vador.validateAndFailFast(
						new IDConfigBeanWithPolymorphicIdFields(new IDConfigID(VALID_CONTACT_ID)), config);
		assertThat(result).isEmpty();
	}

	@DisplayName("When no entityId from DomainSet matches")
	@Test
	void idConfigWithShouldHaveValidSFPolymorphicIdFormatForAllOrFailWith2() {
		final var config =
				ValidationConfig.<IDConfigBeanWithPolymorphicIdFields, ValidationFailure>toValidate()
						.withIdConfig(
								IDConfig
										.<IDConfigID, IDConfigBeanWithPolymorphicIdFields, ValidationFailure, EntityId>
												toValidate()
										.withIdValidator(ValidIdUtil::isThisEntity)
										.shouldHaveValidSFPolymorphicIdFormatOrFailWith(
												Tuple.of(
														IDConfigBeanWithPolymorphicIdFields::getAccountOrContactId,
														List.of(AccountUddConstants.EntityId)),
												INVALID_POLYMORPHIC_UDD_ID))
						.prepare();
		final var result =
				Vador.validateAndFailFast(
						new IDConfigBeanWithPolymorphicIdFields(new IDConfigID(VALID_CONTACT_ID)), config);
		assertThat(result).isPresent().contains(INVALID_POLYMORPHIC_UDD_ID);
	}

	private static class PolymorphicUddFactory implements DomainSetFactory {
		public static final List<EntityId> DOMAIN_SET =
				List.of(AccountUddConstants.EntityId, ContactUddConstants.EntityId);

		@Override
		public List<EntityId> getDomains() {
			return DOMAIN_SET;
		}
	}

	private interface DomainSetFactory {
		List<EntityId> getDomains();
	}

	// tag::bean-strict-id-validation[]
	@Test
	void idConfigForBatch() {
		final var config =
				BatchValidationConfig.<IDConfigBeanWithIdFields2, ValidationFailure>toValidate()
						.withIdConfig(
								IDConfig
										.<IDConfigID, IDConfigBeanWithIdFields2, ValidationFailure, EntityId>
												toValidate()
										.withIdValidator(ValidIdUtil::isThisEntity)
										.shouldHaveValidSFIdFormatOrFailWith(
												Tuple.of(
														IDConfigBeanWithIdFields2::getAccountId, AccountUddConstants.EntityId),
												INVALID_UDD_ID)
										.absentOrHaveValidSFIdFormatOrFailWith(
												Tuple.of(
														IDConfigBeanWithIdFields2::getContactId, ContactUddConstants.EntityId),
												INVALID_OPTIONAL_UDD_ID))
						.prepare();
		final var validBean = new IDConfigBeanWithIdFields2(new IDConfigID(VALID_ACCOUNT_ID), null);
		final var validatables =
				List.of(
						validBean,
						new IDConfigBeanWithIdFields2(new IDConfigID(INVALID_SF_ID), null),
						new IDConfigBeanWithIdFields2(
								new IDConfigID(VALID_ACCOUNT_ID), new IDConfigID(INVALID_SF_ID)));
		final var results = VadorBatch.validateAndFailFastForEach(validatables, config);
		assertThat(results)
				.containsExactly(right(validBean), left(INVALID_UDD_ID), left(INVALID_OPTIONAL_UDD_ID));
	}

	/** Dummy. A core client may use `common.udd.ValidIdUtil.isThisEntity(String, EntityId)` */
	private static class ValidIdUtil {
		// ! NOTE: These should be implemented by the client and passed through `withIdValidator`

		/** Dummy implementation */
		private static boolean isThisEntity(IDConfigID idToValidate, EntityId entityId) {
			final var id = idToValidate.getValue();
			return !(INVALID_SF_ID.equalsIgnoreCase(id) || INVALID_SF_POLYMORPHIC_ID.equalsIgnoreCase(id))
					&& ID_TO_ENTITY_ID.get(id) != null
					&& ID_TO_ENTITY_ID.get(id).isInstance(entityId);
		}

		/** Dummy implementation */
		private static boolean isThisEntity(String idStrToValidate, EntityId entityId) {
			return !(INVALID_SF_ID.equalsIgnoreCase(idStrToValidate)
							|| INVALID_SF_POLYMORPHIC_ID.equalsIgnoreCase(idStrToValidate))
					&& ID_TO_ENTITY_ID.get(idStrToValidate) != null
					&& ID_TO_ENTITY_ID.get(idStrToValidate).isInstance(entityId);
		}
	}

	// end::bean-strict-id-validation[]

	/**
	 * This imitates `common.udd.EntityId` interface from core which is implemented by all Entities
	 */
	interface EntityId {}

	/** This imitates entity UddConstants */
	private static class AccountUddConstants {
		public static final EntityId EntityId = new IDConfigAccountEntityId();
	}

	private static class ContactUddConstants {
		public static final EntityId EntityId = new IDConfigContactEntityId();
	}

	private static class ProductUddConstants {
		public static final EntityId EntityId = new IDConfigProductEntityId();
	}
}
