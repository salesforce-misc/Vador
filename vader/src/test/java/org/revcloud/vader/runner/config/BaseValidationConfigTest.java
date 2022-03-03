package org.revcloud.vader.runner.config;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.revcloud.vader.runner.Vader.validateAndFailFast;
import static sample.consumer.failure.ValidationFailure.FIELD_INTEGRITY_EXCEPTION;
import static sample.consumer.failure.ValidationFailure.INVALID_OPTIONAL_UDD_ID;
import static sample.consumer.failure.ValidationFailure.INVALID_UDD_ID;
import static sample.consumer.failure.ValidationFailure.INVALID_UDD_ID_2;
import static sample.consumer.failure.ValidationFailure.NONE;
import static sample.consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static sample.consumer.failure.ValidationFailure.REQUIRED_FIELD_MISSING;
import static sample.consumer.failure.ValidationFailure.REQUIRED_FIELD_MISSING_1;
import static sample.consumer.failure.ValidationFailure.REQUIRED_FIELD_MISSING_2;
import static sample.consumer.failure.ValidationFailure.REQUIRED_LIST_MISSING;
import static sample.consumer.failure.ValidationFailure.getFailureWithParams;

import com.force.swag.id.ID;
import io.vavr.Tuple;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.BatchValidationConfig;
import org.revcloud.vader.runner.IDConfig;
import org.revcloud.vader.runner.Vader;
import org.revcloud.vader.runner.VaderBatch;
import org.revcloud.vader.runner.ValidationConfig;
import org.revcloud.vader.specs.types.Specs;
import sample.consumer.failure.ValidationFailure;
import sample.consumer.failure.ValidationFailureMessage;

/** Contains tests for all DSL common to configs derived from BaseValidationConfig */
class BaseValidationConfigTest {

  // tag::validationConfig-for-flat-bean-demo[]
  @DisplayName("Cases covered - Missing Field, String Field, List Field")
  @Test
  void failFastWithRequiredFieldsMissing() {
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .shouldHaveFieldsOrFailWith(
                Map.of(
                    Bean::getRequiredField1, REQUIRED_FIELD_MISSING_1,
                    Bean::getRequiredField2, REQUIRED_FIELD_MISSING_2,
                    Bean::getRequiredList, REQUIRED_LIST_MISSING))
            .shouldHaveValidSFIdFormatForAllOrFailWith(
                Map.of(
                    Bean::getSfId1, FIELD_INTEGRITY_EXCEPTION,
                    Bean::getSfId2, FIELD_INTEGRITY_EXCEPTION))
            .withValidatorEtr(
                beanEtr -> beanEtr.filterOrElse(Objects::nonNull, ignore -> NOTHING_TO_VALIDATE))
            .prepare();

    final var validatableWithBlankReqField = new Bean(0, "", null, null, List.of("1"));
    final var result1 = validateAndFailFast(validatableWithBlankReqField, validationConfig);
    assertThat(result1).contains(REQUIRED_FIELD_MISSING_2);

    final var validatableWithNullReqField = new Bean(null, "2", null, null, List.of("1"));
    final var result2 = validateAndFailFast(validatableWithNullReqField, validationConfig);
    assertThat(result2).contains(REQUIRED_FIELD_MISSING_1);

    final var validatableWithEmptyReqList = new Bean(1, "2", null, null, emptyList());
    final var result3 = validateAndFailFast(validatableWithEmptyReqList, validationConfig);
    assertThat(result3).contains(REQUIRED_LIST_MISSING);
  }
  // end::validationConfig-for-flat-bean-demo[]

  @Test
  void failFastWithRequiredFieldMissingFailWithFn() {
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .shouldHaveFieldsOrFailWithFn(
                Tuple.of(
                    List.of(
                        Bean::getRequiredField1, Bean::getRequiredField2, Bean::getRequiredList),
                    (missingFieldName, missingFieldValue) ->
                        getFailureWithParams(
                            REQUIRED_FIELD_MISSING,
                            missingFieldName,
                            missingFieldValue + "missing")))
            .prepare();
    final var expectedFieldNames =
        Set.of(Bean.Fields.requiredField1, Bean.Fields.requiredField2, Bean.Fields.requiredList);
    assertThat(validationConfig.getRequiredFieldNames(Bean.class)).isEqualTo(expectedFieldNames);
    final var withRequiredFieldNull = new Bean(1, "", null, null, emptyList());

    final var result = validateAndFailFast(withRequiredFieldNull, validationConfig);
    assertThat(result).isPresent();
    assertThat(result.get().getValidationFailureMessage().getParams())
        .containsExactly(Bean.Fields.requiredField2, "missing");
  }

  @DisplayName("Cases covered - Optional field missing")
  @Test
  void failFastWithRequiredFieldMissingFailWithFn2() {
    final var validationConfig =
        ValidationConfig.<Bean1, ValidationFailure>toValidate()
            .shouldHaveFieldOrFailWithFn(
                Bean1::getStr,
                (fieldName, value) -> {
                  assertThat(fieldName).isEqualTo(Bean1.Fields.str);
                  return REQUIRED_FIELD_MISSING;
                })
            .prepare();
    var bean1 = new Bean1(Optional.empty());
    final var result = validateAndFailFast(bean1, validationConfig);
    assertThat(result).contains(REQUIRED_FIELD_MISSING);
  }

  @Test
  void failFastWithInvalidId() {
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .shouldHaveFieldsOrFailWith(
                Map.of(
                    Bean::getRequiredField1, REQUIRED_FIELD_MISSING,
                    Bean::getRequiredField2, REQUIRED_FIELD_MISSING))
            .shouldHaveValidSFIdFormatForAllOrFailWith(
                Map.of(
                    Bean::getSfId1, FIELD_INTEGRITY_EXCEPTION,
                    Bean::getSfId2, FIELD_INTEGRITY_EXCEPTION))
            .prepare();
    final var validatableWithInvalidSfId =
        new Bean(0, "1", new ID("1ttxx00000000hZAAQ"), new ID("invalidSfId"), emptyList());
    final var result = validateAndFailFast(validatableWithInvalidSfId, validationConfig);
    assertThat(result).contains(FIELD_INTEGRITY_EXCEPTION);
  }

  @Test
  void failFastWithInvalidIdFailWithFn() {
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .shouldHaveValidSFIdFormatForAllOrFailWithFn(
                Tuple.of(
                    List.of(Bean::getSfId1, Bean::getSfId2),
                    (name, value) ->
                        getFailureWithParams(
                            ValidationFailureMessage.MSG_WITH_PARAMS, name, value)))
            .prepare();
    final var expectedFieldNames = Set.of(Bean.Fields.sfId1, Bean.Fields.sfId2);
    assertThat(validationConfig.getRequiredFieldNamesForSFIdFormat(Bean.class))
        .isEqualTo(expectedFieldNames);
    final var invalidSfId = new ID("invalidSfId");
    final var validatableWithInvalidSfId =
        new Bean(null, null, new ID("1ttxx00000000hZAAQ"), invalidSfId, emptyList());
    final var result = validateAndFailFast(validatableWithInvalidSfId, validationConfig);
    assertThat(result).isPresent();
    assertThat(result.get().getValidationFailureMessage().getParams())
        .containsExactly(Bean.Fields.sfId2, invalidSfId);
  }

  @Test
  void getSpecWithNameWithDuplicateNames() {
    val duplicateSpecName = "DuplicateSpecName";
    final var specsForConfig =
        (Specs<BeanWithIdFields, ValidationFailure>)
            spec ->
                List.of(
                    spec._1()
                        .nameForTest(duplicateSpecName)
                        .given(BeanWithIdFields::getRequiredField),
                    spec._1().nameForTest(duplicateSpecName).given(BeanWithIdFields::getContactId));
    final var validationConfig =
        ValidationConfig.<BeanWithIdFields, ValidationFailure>toValidate()
            .specify(specsForConfig)
            .prepare();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> validationConfig.getPredicateOfSpecForTest(duplicateSpecName));
  }

  @Test
  void getFieldNames() {
    final var validationConfig =
        ValidationConfig.<BeanWithIdFields, ValidationFailure>toValidate()
            .shouldHaveFieldOrFailWith(BeanWithIdFields::getRequiredField, NONE)
            .shouldHaveValidSFIdFormatOrFailWith(BeanWithIdFields::getAccountId, NONE)
            .absentOrHaveValidSFIdFormatOrFailWith(BeanWithIdFields::getContactId, NONE)
            .prepare();
    assertThat(validationConfig.getRequiredFieldNames(BeanWithIdFields.class))
        .contains(BeanWithIdFields.Fields.requiredField);
    assertThat(validationConfig.getRequiredFieldNamesForSFIdFormat(BeanWithIdFields.class))
        .contains(BeanWithIdFields.Fields.accountId);
    assertThat(validationConfig.getNonRequiredFieldNamesForSFIdFormat(BeanWithIdFields.class))
        .contains(BeanWithIdFields.Fields.contactId);
  }

  // tag::validationConfig-for-nested-bean-demo[]
  @Test
  void nestedBeanValidationWithInvalidMember() {
    final var memberValidationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .shouldHaveValidSFIdFormatForAllOrFailWithFn(
                Tuple.of(
                    List.of(Bean::getSfId1, Bean::getSfId2),
                    (name, value) ->
                        getFailureWithParams(
                            ValidationFailureMessage.MSG_WITH_PARAMS, name, value)))
            .prepare();
    final var containerValidationConfig =
        ValidationConfig.<ContainerBean, ValidationFailure>toValidate()
            .shouldHaveFieldOrFailWithFn(
                ContainerBean::getRequiredField,
                (name, value) ->
                    getFailureWithParams(ValidationFailureMessage.MSG_WITH_PARAMS, name, value))
            .prepare();

    final var invalidSfId = new ID("invalidSfId");
    final var memberWithInvalidSfId =
        new Bean(null, null, new ID("1ttxx00000000hZAAQ"), invalidSfId, emptyList());
    final var validContainer = new ContainerBean("requiredField", memberWithInvalidSfId);
    final var result =
        validateAndFailFast(validContainer, containerValidationConfig)
            .or(() -> validateAndFailFast(memberWithInvalidSfId, memberValidationConfig));

    assertThat(result).isPresent();
    assertThat(result.get().getValidationFailureMessage().getParams())
        .containsExactly(Bean.Fields.sfId2, invalidSfId);
  }
  // end::validationConfig-for-nested-bean-demo[]

  private static final String VALID_SF_ACCOUNT_ID = "001xx000003GYQxAAO";

  // ! TODO gopala.akshintala 03/03/22: Extract IDConfig tests to different file.
  @Test
  void idConfigWithShouldHaveValidSFIdFormatForAllOrFailWithFn() {
    final var config =
        ValidationConfig.<BeanWithIdFields, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<ID, BeanWithIdFields, ValidationFailure, EntityId>toValidate()
                    .withIdValidator(ValidIdUtil::isThisEntity)
                    .shouldHaveValidSFIdFormatForAllOrFailWithFn(
                        Tuple.of(
                            Map.of(
                                BeanWithIdFields::getAccountId, AccountUddConstants.EntityId,
                                BeanWithIdFields::getContactId, ContactUddConstants.EntityId),
                            (invalidIdFieldName, invalidIdFieldValue) ->
                                getFailureWithParams(
                                    INVALID_UDD_ID, invalidIdFieldName, invalidIdFieldValue))))
            .prepare();
    final var invalidContactId = new ID("invalidSFId");
    final var result =
        Vader.validateAndFailFast(
            new BeanWithIdFields(null, new ID(VALID_SF_ACCOUNT_ID), invalidContactId), config);
    assertThat(result).isPresent().contains(INVALID_UDD_ID);
    assertThat(result.get().getValidationFailureMessage().getParams())
        .containsExactly("contactId", invalidContactId);
  }

  @Test
  void idConfigWithShouldHaveValidSFIdFormatForAllOrFailWith() {
    final var config =
        ValidationConfig.<BeanWithIdFields, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<ID, BeanWithIdFields, ValidationFailure, EntityId>toValidate()
                    .withIdValidator(ValidIdUtil::isThisEntity)
                    .shouldHaveValidSFIdFormatForAllOrFailWith(
                        Map.of(
                            Tuple.of(BeanWithIdFields::getAccountId, AccountUddConstants.EntityId),
                                getFailureWithParams(INVALID_UDD_ID, "accountId"),
                            Tuple.of(BeanWithIdFields::getContactId, ContactUddConstants.EntityId),
                                getFailureWithParams(INVALID_UDD_ID_2, "contactId"))))
            .prepare();
    final var invalidContactId = new ID("invalidSFId");
    final var result =
        Vader.validateAndFailFast(
            new BeanWithIdFields(null, new ID(VALID_SF_ACCOUNT_ID), invalidContactId), config);
    assertThat(result).isPresent().contains(INVALID_UDD_ID_2);
    assertThat(result.get().getValidationFailureMessage().getParams()).containsExactly("contactId");
  }

  @Test
  @DisplayName("Not providing withValidator leads to the usage of Fallback validator")
  void idConfigWithFallBackValidator() {
    final var config =
        ValidationConfig.<BeanWithIdFields, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<ID, BeanWithIdFields, ValidationFailure, EntityId>toValidate()
                    .shouldHaveValidSFIdFormatForAllOrFailWith(
                        Map.of(
                            Tuple.of(BeanWithIdFields::getAccountId, AccountUddConstants.EntityId),
                                INVALID_UDD_ID,
                            Tuple.of(BeanWithIdFields::getContactId, ContactUddConstants.EntityId),
                                INVALID_UDD_ID_2)))
            .prepare();
    final var result =
        Vader.validateAndFailFast(
            new BeanWithIdFields(null, new ID(VALID_SF_ACCOUNT_ID), new ID("InvalidSFId")), config);
    assertThat(result).contains(INVALID_UDD_ID_2);
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
        Vader.validateAndFailFast(new BeanWithIdStrFields(null, "invalidSFId", null), config);
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
            new BeanWithMixIdFields(null, new ID("invalidSFId"), null), config);
    assertThat(result).contains(INVALID_UDD_ID);
  }

  // tag::bean-strict-id-validation[]
  @Test
  void idConfigForBatch() {
    final var config =
        BatchValidationConfig.<BeanWithIdFields, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<ID, BeanWithIdFields, ValidationFailure, EntityId>toValidate()
                    .withIdValidator(ValidIdUtil::isThisEntity)
                    .shouldHaveValidSFIdFormatOrFailWith(
                        Tuple.of(BeanWithIdFields::getAccountId, AccountUddConstants.EntityId),
                        INVALID_UDD_ID)
                    .absentOrHaveValidSFIdFormatOrFailWith(
                        Tuple.of(BeanWithIdFields::getContactId, ContactUddConstants.EntityId),
                        INVALID_OPTIONAL_UDD_ID))
            .prepare();
    final var validBean = new BeanWithIdFields(null, new ID("validId"), null);
    final var validatables =
        List.of(
            validBean,
            new BeanWithIdFields(null, new ID("invalidSFId"), null),
            new BeanWithIdFields(null, new ID("validId"), new ID("invalidSFId")));
    final var results = VaderBatch.validateAndFailFastForEach(validatables, config);
    assertThat(results)
        .containsExactly(right(validBean), left(INVALID_UDD_ID), left(INVALID_OPTIONAL_UDD_ID));
  }

  /** Dummy. A core client may use `common.udd.ValidIdUtil.isThisEntity(String, EntityId)` */
  private static class ValidIdUtil {
    // ! NOTE: These should be implemented by the client and passed through `withIdValidator`

    // ! TODO gopala.akshintala 03/03/22: Enhance this dummy to check prefixes and update
    // documentation
    private static boolean isThisEntity(ID idToValidate, EntityId entityId) {
      return !idToValidate.toString().equalsIgnoreCase("invalidSFId"); // dummy implementation
    }

    private static boolean isThisEntity(String idToValidate, EntityId entityId) {
      return !idToValidate.equalsIgnoreCase("invalidSFId"); // dummy implementation
    }
  }
  // end::bean-strict-id-validation[]

  @Data
  @AllArgsConstructor
  @FieldNameConstants
  // tag::bean-with-id-fields[]
  public static class BeanWithIdFields {
    String requiredField;
    ID accountId;
    ID contactId;
  }

  /**
   * This imitates `common.udd.EntityId` interface from core which is implemented by all Entities.
   */
  private interface EntityId {}

  @Value
  private static class AccountEntityId implements EntityId {}

  @Value
  private static class ContactEntityId implements EntityId {}
  // end::bean-with-id-fields[]

  @Data
  @AllArgsConstructor
  @FieldNameConstants
  public static class BeanWithIdStrFields {
    String requiredField;
    String accountId;
    String contactId;
  }

  @Data
  @AllArgsConstructor
  @FieldNameConstants
  public static class BeanWithMixIdFields {
    String requiredField;
    ID accountId;
    String contactId;
  }

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  // tag::nested-bean[]
  // tag::flat-bean[]
  public static class Bean {
    private final Integer requiredField1;
    private final String requiredField2;
    private final ID sfId1;
    private final ID sfId2;
    private final List<String> requiredList;
  }
  // end::flat-bean[]
  // end::nested-bean[]

  @Value
  // tag::nested-bean[]
  private static class ContainerBean {
    String requiredField;
    Bean bean;
  }
  // end::nested-bean[]

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  public static class Bean1 {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<String> str;
  }

  private static class AccountUddConstants {
    public static final EntityId EntityId = new AccountEntityId();
  }

  private static class ContactUddConstants {
    public static final EntityId EntityId = new ContactEntityId();
  }
}
