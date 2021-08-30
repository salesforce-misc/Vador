package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.INVALID_UDD_ID;
import static consumer.failure.ValidationFailure.NONE;
import static org.assertj.core.api.Assertions.assertThat;

import com.force.swag.id.ID;
import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.experimental.FieldNameConstants;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.BaseValidationConfigTest.Bean.Fields;
import org.revcloud.vader.specs.types.Specs;

class BaseValidationConfigTest {

  @Test
  void getSpecWithNameWithDuplicateNames() {
    val duplicateSpecName = "DuplicateSpecName";
    final var specsForConfig =
        (Specs<Bean, ValidationFailure>)
            spec ->
                List.of(
                    spec._1().nameForTest(duplicateSpecName).given(Bean::getRequiredField),
                    spec._1()
                        .nameForTest(duplicateSpecName)
                        .given(Bean::getOptionalSfIdFormatField));
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate().specify(specsForConfig).prepare();
    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> validationConfig.getPredicateOfSpecForTest(duplicateSpecName));
  }

  @Test
  void getFieldNames() {
    final var validationConfig =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .shouldHaveFieldOrFailWith(Bean::getRequiredField, NONE)
            .shouldHaveValidSFIdFormatOrFailWith(Bean::getSfIdFormatField, NONE)
            .absentOrHaveValidSFIdFormatOrFailWith(Bean::getOptionalSfIdFormatField, NONE)
            .prepare();
    assertThat(validationConfig.getRequiredFieldNames(Bean.class)).contains(Fields.requiredField);
    assertThat(validationConfig.getRequiredFieldNamesForSFIdFormat(Bean.class))
        .contains(Fields.sfIdFormatField);
    assertThat(validationConfig.getNonRequiredFieldNamesForSFIdFormat(Bean.class))
        .contains(Fields.optionalSfIdFormatField);
  }

  @Test
  void idConfig() {
    final var entityInfo = new EntityInfo();
    final var config =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<Bean, ValidationFailure, EntityInfo>toValidate()
                    .withIdValidator(BaseValidationConfigTest::uddUtil)
                    .shouldHaveValidSFIdFormatOrFailWith(
                        Tuple.of(Bean::getSfIdFormatField, entityInfo, INVALID_UDD_ID))
                    .prepare())
            .prepare();
    final var result = Vader.validateAndFailFast(new Bean(null, new ID("invalidId"), null), config);
    assertThat(result).contains(INVALID_UDD_ID);
  }

  private static boolean uddUtil(ID idToValidate, EntityInfo entityInfo) {
    return !idToValidate.toString().equalsIgnoreCase("invalidId");
  }

  @Data
  @AllArgsConstructor
  @FieldNameConstants
  // tag::flat-bean[]
  public static class Bean {
    String requiredField;
    ID sfIdFormatField;
    ID optionalSfIdFormatField;
  }
  // end::flat-bean[]

  @Value
  private static class EntityInfo {}
}
