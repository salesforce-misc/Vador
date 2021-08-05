package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.FIELD_INTEGRITY_EXCEPTION;
import static consumer.failure.ValidationFailure.REQUIRED_FIELD_MISSING;
import static consumer.failure.ValidationFailure.REQUIRED_FIELD_MISSING_1;
import static consumer.failure.ValidationFailure.REQUIRED_FIELD_MISSING_2;
import static consumer.failure.ValidationFailure.REQUIRED_LIST_MISSING;
import static consumer.failure.ValidationFailure.getFailureWithParams;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.revcloud.vader.runner.Runner.validateAndFailFast;

import com.force.swag.id.ID;
import consumer.failure.ValidationFailure;
import consumer.failure.ValidationFailureMessage;
import io.vavr.Tuple;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ValidationConfigTest {

  @DisplayName("Cases covered - Missing Field, String Field, List Field")
  @Test
  void failFastWithRequiredFieldsMissings() {
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
            .prepare();

    final var validatableWithBlankReqField = new Bean(0, "", null, null, List.of("1"));
    final var result1 =
        validateAndFailFast(
            validatableWithBlankReqField,
            validationConfig);
    assertThat(result1).contains(REQUIRED_FIELD_MISSING_2);

    final var validatableWithNullReqField = new Bean(null, "2", null, null, List.of("1"));
    final var result2 =
        validateAndFailFast(
            validatableWithNullReqField,
            validationConfig);
    assertThat(result2).contains(REQUIRED_FIELD_MISSING_1);

    final var validatableWithEmptyReqList = new Bean(1, "2", null, null, emptyList());
    final var result3 =
        validateAndFailFast(
            validatableWithEmptyReqList,
            validationConfig);
    assertThat(result3).contains(REQUIRED_LIST_MISSING);
  }

  @DisplayName("Cases covered - Optional field missing")
  @Test
  void failFastWithRequiredFieldMissing2() {
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
    final var result =
        validateAndFailFast(bean1, validationConfig);
    assertThat(result).contains(REQUIRED_FIELD_MISSING);
  }

  @Test
  void failFastWithRequiredFieldsWithNameMissingForValidators() {
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .shouldHaveFieldsOrFailWithFn(
                Tuple.of(
                    List.of(Bean::getRequiredField1, Bean::getRequiredField2),
                    (name, value) ->
                        getFailureWithParams(
                            ValidationFailureMessage.MSG_WITH_PARAMS, name, value)))
            .prepare();
    final var expectedFieldNames = Set.of(Bean.Fields.requiredField1, Bean.Fields.requiredField2);
    assertThat(validationConfig.getRequiredFieldNames(Bean.class)).isEqualTo(expectedFieldNames);
    final var withRequiredFieldNull = new Bean(1, "", null, null, emptyList());
    final var result =
        validateAndFailFast(withRequiredFieldNull, validationConfig);
    assertThat(result).isPresent();
    assertThat(result.get().getValidationFailureMessage().getParams())
        .containsExactly(Bean.Fields.requiredField2, "");
  }

  @Test
  void failFastWithInvalidIdForValidators() {
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
    final var result =
        validateAndFailFast(validatableWithInvalidSfId, validationConfig);
    assertThat(result).contains(FIELD_INTEGRITY_EXCEPTION);
  }

  @Test
  void failFastWithInvalidIdWithNameForValidators() {
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
    final var result =
        validateAndFailFast(
            validatableWithInvalidSfId,
            validationConfig);
    assertThat(result).isPresent();
    assertThat(result.get().getValidationFailureMessage().getParams())
        .containsExactly(Bean.Fields.sfId2, invalidSfId);
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
        validateAndFailFast(
                validContainer,
                containerValidationConfig,
                ValidationFailure::getValidationFailureForException)
            .or(
                () ->
                    validateAndFailFast(
                        memberWithInvalidSfId,
                        memberValidationConfig,
                        ValidationFailure::getValidationFailureForException));

    assertThat(result).isPresent();
    assertThat(result.get().getValidationFailureMessage().getParams())
        .containsExactly(Bean.Fields.sfId2, invalidSfId);
  }
  // end::validationConfig-for-nested-bean-demo[]

  @Data
  @FieldNameConstants
  @AllArgsConstructor
  // tag::nested-bean[]
  public static class Bean {
    private final Integer requiredField1;
    private final String requiredField2;
    private final ID sfId1;
    private final ID sfId2;
    private final List<String> requiredList;
  }
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
    Optional<String> str;
  }
}
