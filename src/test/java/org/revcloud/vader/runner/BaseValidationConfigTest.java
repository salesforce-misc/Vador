package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.NONE;
import static org.assertj.core.api.Assertions.assertThat;

import com.force.swag.id.ID;
import consumer.failure.ValidationFailure;
import java.util.List;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.BaseValidationConfigTest.Bean.Fields;

class BaseValidationConfigTest {

  @Test
  void getSpecWithNameWithDuplicateNames() {
    val duplicateSpecName = "DuplicateSpecName";
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .specify(
                spec ->
                    List.of(
                        spec._1().nameForTest(duplicateSpecName).given(Bean::getRequiredField),
                        spec._1().nameForTest(duplicateSpecName).given(Bean::getOptionalSfIdFormatField)))
            .prepare();
    Assertions.assertThrows(
        IllegalArgumentException.class, () -> validationConfig.getSpecWithName(duplicateSpecName));
  }

  @Test
  void getFieldNames() {
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .shouldHaveFieldOrFailWith(Bean::getRequiredField, NONE)
            .shouldHaveValidSFIdFormatOrFailWith(Bean::getSfIdFormatField,NONE)
            .absentOrHaveValidSFIdFieldsOrFailWith(Bean::getOptionalSfIdFormatField, NONE)
            .prepare();
    assertThat(validationConfig.getRequiredFieldNames(Bean.class)).contains(Fields.requiredField);
    assertThat(validationConfig.getRequiredFieldNamesForSFIdFormat(Bean.class)).contains(Fields.sfIdFormatField);
    assertThat(validationConfig.getNonRequiredFieldNamesForSFIdFormat(Bean.class)).contains(Fields.optionalSfIdFormatField);
  }

  @Data
  @FieldNameConstants
  public static class Bean {
    String requiredField;
    ID sfIdFormatField;
    ID optionalSfIdFormatField;
  }
}
