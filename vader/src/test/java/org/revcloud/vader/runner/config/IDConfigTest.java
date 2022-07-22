/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.runner.config;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static org.assertj.core.api.Assertions.assertThat;
import static sample.consumer.failure.ValidationFailure.INVALID_OPTIONAL_UDD_ID;
import static sample.consumer.failure.ValidationFailure.INVALID_POLYMORPHIC_UDD_ID;
import static sample.consumer.failure.ValidationFailure.INVALID_UDD_ID;
import static sample.consumer.failure.ValidationFailure.INVALID_UDD_ID_2;
import static sample.consumer.failure.ValidationFailure.INVALID_UDD_ID_3;
import static sample.consumer.failure.ValidationFailure.getFailureWithParams;

import io.vavr.Tuple;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.BatchValidationConfig;
import org.revcloud.vader.runner.IDConfig;
import org.revcloud.vader.runner.Vader;
import org.revcloud.vader.runner.VaderBatch;
import org.revcloud.vader.runner.ValidationConfig;
import org.revcloud.vader.runner.config.BaseValidationConfigTest.BeanWithIdStrFields;
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
          VALID_ACCOUNT_ID, AccountEntityId.class,
          VALID_CONTACT_ID, ContactEntityId.class);

  @Test
  void idConfigWithShouldHaveValidSFIdFormatForAllOrFailWithFn() {
    final var config =
        ValidationConfig.<BeanWithIdFields2, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<ID, BeanWithIdFields2, ValidationFailure, EntityId>toValidate()
                    .withIdValidator(ValidIdUtil::isThisEntity)
                    .shouldHaveValidSFIdFormatForAllOrFailWithFn(
                        Tuple.of(
                            Map.of(
                                BeanWithIdFields2::getAccountId, AccountUddConstants.EntityId,
                                BeanWithIdFields2::getContactId, ContactUddConstants.EntityId),
                            (invalidIdFieldName, invalidIdFieldValue) ->
                                getFailureWithParams(
                                    INVALID_UDD_ID, invalidIdFieldName, invalidIdFieldValue))))
            .prepare();
    final var invalidContactId = new ID(INVALID_SF_ID);
    final var result =
        Vader.validateAndFailFast(
            new BeanWithIdFields2(new ID(VALID_ACCOUNT_ID), invalidContactId), config);
    assertThat(result).isPresent().contains(INVALID_UDD_ID);
    assertThat(result.get().getValidationFailureMessage().getParams())
        .containsExactly(CONTACT_ID, invalidContactId);
  }

  @Test
  void idConfigWithShouldHaveValidSFIdFormatForAllOrFailWith() {
    final var config =
        ValidationConfig.<BeanWithIdFields2, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<ID, BeanWithIdFields2, ValidationFailure, EntityId>toValidate()
                    .withIdValidator(ValidIdUtil::isThisEntity)
                    .shouldHaveValidSFIdFormatForAllOrFailWith(
                        Map.of(
                            Tuple.of(BeanWithIdFields2::getAccountId, AccountUddConstants.EntityId),
                            getFailureWithParams(INVALID_UDD_ID, ACCOUNT_ID),
                            Tuple.of(BeanWithIdFields2::getContactId, ContactUddConstants.EntityId),
                            getFailureWithParams(INVALID_UDD_ID_2, CONTACT_ID))))
            .prepare();
    final var invalidContactId = new ID(INVALID_SF_ID);

    final var result =
        Vader.validateAndFailFast(
            new BeanWithIdFields2(new ID(VALID_ACCOUNT_ID), invalidContactId), config);
    assertThat(result).isPresent().contains(INVALID_UDD_ID_2);
    assertThat(result.get().getValidationFailureMessage().getParams()).containsExactly(CONTACT_ID);
  }

  @DisplayName(
      "IdConfig With `shouldHaveValidSFIdFormatForAllOrFailWith` And `AbsentOrHaveValidSFIdFormatOrFailWith`")
  @Test
  void idConfigWithMultipleConditions() {
    final var config =
        ValidationConfig.<BeanWithIdFields3, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<ID, BeanWithIdFields3, ValidationFailure, EntityId>toValidate()
                    .withIdValidator(ValidIdUtil::isThisEntity)
                    .shouldHaveValidSFIdFormatForAllOrFailWith(
                        Map.of(
                            Tuple.of(BeanWithIdFields3::getAccountId, AccountUddConstants.EntityId),
                            getFailureWithParams(INVALID_UDD_ID, ACCOUNT_ID),
                            Tuple.of(BeanWithIdFields3::getContactId, ContactUddConstants.EntityId),
                            getFailureWithParams(INVALID_UDD_ID_2, CONTACT_ID)))
                    .absentOrHaveValidSFIdFormatOrFailWith(
                        Tuple.of(BeanWithIdFields3::getProductId, ProductUddConstants.EntityId),
                        getFailureWithParams(INVALID_UDD_ID_3, PRODUCT_ID)))
            .prepare();
    final var invalidProductId = new ID(INVALID_SF_ID);
    final var result =
        Vader.validateAndFailFast(
            new BeanWithIdFields3(
                new ID(VALID_ACCOUNT_ID), new ID(VALID_CONTACT_ID), invalidProductId),
            config);
    assertThat(result).isPresent().contains(INVALID_UDD_ID_3);
    assertThat(result.get().getValidationFailureMessage().getParams()).containsExactly(PRODUCT_ID);
  }

  @Test
  void idConfigWithStrIds() {
    final var config =
        ValidationConfig.<BeanWithIdStrFields, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<String, BeanWithIdStrFields, ValidationFailure, EntityId>toValidate()
                    .withIdValidator(ValidIdUtil::isThisEntity)
                    .shouldHaveValidSFIdFormatForAllOrFailWithFn(
                        Tuple.of(
                            Map.of(
                                BeanWithIdStrFields::getAccountId, AccountUddConstants.EntityId,
                                BeanWithIdStrFields::getContactId, ContactUddConstants.EntityId),
                            (invalidIdFieldName, invalidIdFieldValue) ->
                                getFailureWithParams(
                                    INVALID_UDD_ID, invalidIdFieldName, invalidIdFieldValue))))
            .prepare();
    final var result =
        Vader.validateAndFailFast(new BeanWithIdStrFields(null, INVALID_SF_ID, null), config);
    assertThat(result).contains(INVALID_UDD_ID);
  }

  @Test
  void idConfigWithMixOfIdsAndStrIds() {
    final var config =
        ValidationConfig.<BeanWithMixIdFields, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<ID, BeanWithMixIdFields, ValidationFailure, EntityId>toValidate()
                    .withIdValidator(ValidIdUtil::isThisEntity)
                    .shouldHaveValidSFIdFormatOrFailWith(
                        Tuple.of(BeanWithMixIdFields::getAccountId, AccountUddConstants.EntityId),
                        INVALID_UDD_ID))
            .withIdConfig(
                IDConfig.<String, BeanWithMixIdFields, ValidationFailure, EntityId>toValidate()
                    .withIdValidator(ValidIdUtil::isThisEntity)
                    .absentOrHaveValidSFIdFormatOrFailWith(
                        Tuple.of(BeanWithMixIdFields::getContactId, ContactUddConstants.EntityId),
                        INVALID_OPTIONAL_UDD_ID))
            .prepare();
    final var result =
        Vader.validateAndFailFast(
            new BeanWithMixIdFields(null, new ID(INVALID_SF_ID), null), config);
    assertThat(result).contains(INVALID_UDD_ID);
  }

  @Test
  void idConfigWithShouldHaveValidSFPolymorphicIdFormatForAllOrFailWith() {
    final var config =
        ValidationConfig.<BeanWithPolymorphicIdFields, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<ID, BeanWithPolymorphicIdFields, ValidationFailure, EntityId>toValidate()
                    .withIdValidator(ValidIdUtil::isThisEntity)
                    .shouldHaveValidSFPolymorphicIdFormatOrFailWith(
                        Tuple.of(
                            BeanWithPolymorphicIdFields::getAccountOrContactId,
                            PolymorphicUddFactory.DOMAIN_SET),
                        INVALID_POLYMORPHIC_UDD_ID))
            .prepare();
    final var result =
        Vader.validateAndFailFast(
            new BeanWithPolymorphicIdFields(new ID(VALID_CONTACT_ID)), config);
    assertThat(result).isEmpty();
  }

  @DisplayName("When no entityId from DomainSet matches")
  @Test
  void idConfigWithShouldHaveValidSFPolymorphicIdFormatForAllOrFailWith2() {
    final var config =
        ValidationConfig.<BeanWithPolymorphicIdFields, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<ID, BeanWithPolymorphicIdFields, ValidationFailure, EntityId>toValidate()
                    .withIdValidator(ValidIdUtil::isThisEntity)
                    .shouldHaveValidSFPolymorphicIdFormatOrFailWith(
                        Tuple.of(
                            BeanWithPolymorphicIdFields::getAccountOrContactId,
                            List.of(AccountUddConstants.EntityId)),
                        INVALID_POLYMORPHIC_UDD_ID))
            .prepare();
    final var result =
        Vader.validateAndFailFast(
            new BeanWithPolymorphicIdFields(new ID(VALID_CONTACT_ID)), config);
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
        BatchValidationConfig.<BeanWithIdFields2, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<ID, BeanWithIdFields2, ValidationFailure, EntityId>toValidate()
                    .withIdValidator(ValidIdUtil::isThisEntity)
                    .shouldHaveValidSFIdFormatOrFailWith(
                        Tuple.of(BeanWithIdFields2::getAccountId, AccountUddConstants.EntityId),
                        INVALID_UDD_ID)
                    .absentOrHaveValidSFIdFormatOrFailWith(
                        Tuple.of(BeanWithIdFields2::getContactId, ContactUddConstants.EntityId),
                        INVALID_OPTIONAL_UDD_ID))
            .prepare();
    final var validBean = new BeanWithIdFields2(new ID(VALID_ACCOUNT_ID), null);
    final var validatables =
        List.of(
            validBean,
            new BeanWithIdFields2(new ID(INVALID_SF_ID), null),
            new BeanWithIdFields2(new ID(VALID_ACCOUNT_ID), new ID(INVALID_SF_ID)));
    final var results = VaderBatch.validateAndFailFastForEach(validatables, config);
    assertThat(results)
        .containsExactly(right(validBean), left(INVALID_UDD_ID), left(INVALID_OPTIONAL_UDD_ID));
  }

  /** Dummy. A core client may use `common.udd.ValidIdUtil.isThisEntity(String, EntityId)` */
  private static class ValidIdUtil {
    // ! NOTE: These should be implemented by the client and passed through `withIdValidator`

    /** Dummy implementation */
    private static boolean isThisEntity(ID idToValidate, EntityId entityId) {
      final var id = idToValidate.value;
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

  @Data
  @AllArgsConstructor
  @FieldNameConstants
  // tag::bean-with-id-fields[]
  public static class BeanWithIdFields2 {
    ID accountId;
    ID contactId;
  }

  /**
   * This imitates `common.udd.EntityId` interface from core which is implemented by all Entities
   */
  private interface EntityId {}

  @Value
  private static class AccountEntityId implements EntityId {}

  @Value
  private static class ContactEntityId implements EntityId {}

  /** This imitates entity UddConstants */
  private static class AccountUddConstants {
    public static final EntityId EntityId = new AccountEntityId();
  }

  private static class ContactUddConstants {
    public static final EntityId EntityId = new ContactEntityId();
  }
  // end::bean-with-id-fields[]

  @Value
  private static class ProductEntityId implements EntityId {}

  private static class ProductUddConstants {
    public static final EntityId EntityId = new ProductEntityId();
  }

  @Data
  @AllArgsConstructor
  @FieldNameConstants
  public static class BeanWithIdFields3 {
    ID accountId;
    ID contactId;
    ID productId;
  }

  @Data
  @AllArgsConstructor
  @FieldNameConstants
  public static class BeanWithPolymorphicIdFields {
    ID accountOrContactId;
  }

  @Data
  @AllArgsConstructor
  @FieldNameConstants
  public static class BeanWithMixIdFields {
    String requiredField;
    ID accountId;
    String contactId;
  }

  @Value
  private static class ID {
    String value;
  }
}
