package org.revcloud.vader.runner;

import static consumer.failure.ValidationFailure.INVALID_OPTIONAL_UDD_ID;
import static consumer.failure.ValidationFailure.INVALID_UDD_ID;
import static consumer.failure.ValidationFailure.NONE;
import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
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
            .shouldHaveValidSFIdFormatOrFailWith(Bean::getSfIdFormatField1, NONE)
            .absentOrHaveValidSFIdFormatOrFailWith(Bean::getOptionalSfIdFormatField, NONE)
            .prepare();
    assertThat(validationConfig.getRequiredFieldNames(Bean.class)).contains(Fields.requiredField);
    assertThat(validationConfig.getRequiredFieldNamesForSFIdFormat(Bean.class))
        .contains(Fields.sfIdFormatField1);
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
                        Tuple.of(Bean::getSfIdFormatField1, entityInfo, INVALID_UDD_ID))
                    .prepare())
            .prepare();
    final var result = Vader.validateAndFailFast(new Bean(null, new ID("invalidId"), null), config);
    assertThat(result).contains(INVALID_UDD_ID);
  }

  @Test
  void idConfigForBatch() {
    final var entityInfo = new EntityInfo();
    final var config =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<Bean, ValidationFailure, EntityInfo>toValidate()
                    .withIdValidator(BaseValidationConfigTest::uddUtil)
                    .shouldHaveValidSFIdFormatOrFailWith(
                        Tuple.of(Bean::getSfIdFormatField1, entityInfo, INVALID_UDD_ID))
                    .absentOrHaveValidSFIdFormatOrFailWith(
                        Tuple.of(
                            Bean::getOptionalSfIdFormatField, entityInfo, INVALID_OPTIONAL_UDD_ID))
                    .prepare())
            .prepare();
    final var validBean = new Bean(null, new ID("validId"), null);
    final var validatables =
        List.of(
            validBean,
            new Bean(null, new ID("invalidId"), null),
            new Bean(null, new ID("validId"), new ID("invalidId")));
    final var results = VaderBatch.validateAndFailFastForEach(validatables, config);
    assertThat(results)
        .containsExactly(right(validBean), left(INVALID_UDD_ID), left(INVALID_OPTIONAL_UDD_ID));
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
    ID sfIdFormatField1;
    ID optionalSfIdFormatField;
  }
  // end::flat-bean[]

  @Value
  private static class EntityInfo {}
}
