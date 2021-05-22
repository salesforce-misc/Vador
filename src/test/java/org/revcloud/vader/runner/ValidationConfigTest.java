package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.FIELD_INTEGRITY_EXCEPTION;
import static consumer.failure.ValidationFailure.REQUIRED_FIELD_MISSING;
import static consumer.failure.ValidationFailure.getFailureWithParams;
import static org.assertj.core.api.Assertions.assertThat;

import com.force.swag.id.ID;
import consumer.failure.ValidationFailure;
import consumer.failure.ValidationFailureMessage;
import io.vavr.Tuple;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.Test;

class ValidationConfigTest {

  @Test
  void failFastWithRequiredFieldsMissingForSimpleValidators() {
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .shouldHaveFieldsOrFailWith(
                Map.of(
                    Bean::getRequiredField1, REQUIRED_FIELD_MISSING,
                    Bean::getRequiredField2, REQUIRED_FIELD_MISSING))
            .shouldHaveValidSFIdFormatOrFailWith(
                Map.of(
                    Bean::getSfId1, FIELD_INTEGRITY_EXCEPTION,
                    Bean::getSfId2, FIELD_INTEGRITY_EXCEPTION))
            .prepare();

    final var validatableWithBlankReqField = new Bean(0, "", null, null);
    final var result1 =
        Runner.validateAndFailFast(
            validatableWithBlankReqField,
            ValidationFailure::getValidationFailureForException,
            validationConfig);
    assertThat(result1).contains(REQUIRED_FIELD_MISSING);

    final var validatableWithNullReqField = new Bean(0, null, null, null);
    final var result2 =
        Runner.validateAndFailFast(
            validatableWithNullReqField,
            ValidationFailure::getValidationFailureForException,
            validationConfig);
    assertThat(result2).contains(REQUIRED_FIELD_MISSING);
  }

  @Test
  void failFastWithRequiredFieldsWithNameMissingForSimpleValidators() {
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
    final var withRequiredFieldNull = new Bean(1, "", null, null);
    final var result =
        Runner.validateAndFailFast(
            withRequiredFieldNull,
            ValidationFailure::getValidationFailureForException,
            validationConfig);
    assertThat(result).isPresent();
    assertThat(result.get().getValidationFailureMessage().getParams())
        .containsExactly(Bean.Fields.requiredField2, "");
  }

  @Test
  void failFastWithInvalidIdForSimpleValidators() {
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .shouldHaveFieldsOrFailWith(
                Map.of(
                    Bean::getRequiredField1, REQUIRED_FIELD_MISSING,
                    Bean::getRequiredField2, REQUIRED_FIELD_MISSING))
            .shouldHaveValidSFIdFormatOrFailWith(
                Map.of(
                    Bean::getSfId1, FIELD_INTEGRITY_EXCEPTION,
                    Bean::getSfId2, FIELD_INTEGRITY_EXCEPTION))
            .prepare();

    final var validatableWithInvalidSfId =
        new Bean(0, "1", new ID("1ttxx00000000hZAAQ"), new ID("invalidSfId"));
    final var result1 =
        Runner.validateAndFailFast(
            validatableWithInvalidSfId,
            ValidationFailure::getValidationFailureForException,
            validationConfig);
    assertThat(result1).contains(FIELD_INTEGRITY_EXCEPTION);
  }

  @Test
  void failFastWithInvalidIdWithNameForSimpleValidators() {
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .shouldHaveValidSFIdFormatOrFailWithFn(
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
        new Bean(null, null, new ID("1ttxx00000000hZAAQ"), invalidSfId);
    final var result =
        Runner.validateAndFailFast(
            validatableWithInvalidSfId,
            ValidationFailure::getValidationFailureForException,
            validationConfig);
    assertThat(result).isPresent();
    assertThat(result.get().getValidationFailureMessage().getParams())
        .containsExactly(Bean.Fields.sfId2, invalidSfId);
  }

  @Data
  @FieldNameConstants
  public static class Bean {
    private final Integer requiredField1;
    private final String requiredField2;
    private final ID sfId1;
    private final ID sfId2;
  }
}
