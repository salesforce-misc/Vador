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
                        .given(Bean::getOptionalSfIdFormatField2));
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
            .absentOrHaveValidSFIdFormatOrFailWith(Bean::getOptionalSfIdFormatField2, NONE)
            .prepare();
    assertThat(validationConfig.getRequiredFieldNames(Bean.class)).contains(Fields.requiredField);
    assertThat(validationConfig.getRequiredFieldNamesForSFIdFormat(Bean.class))
        .contains(Fields.sfIdFormatField1);
    assertThat(validationConfig.getNonRequiredFieldNamesForSFIdFormat(Bean.class))
        .contains(Fields.optionalSfIdFormatField2);
  }

  @Test
  void idConfig() {
    final var field1EntityInfo = new Field1EntityInfo();
    final var config =
        ValidationConfig.<Bean, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<Bean, ValidationFailure, Field1EntityInfo>toValidate()
                    .withIdValidator(BaseValidationConfigTest::uddUtil)
                    .shouldHaveValidSFIdFormatOrFailWith(
                        Tuple.of(Bean::getSfIdFormatField1, field1EntityInfo, INVALID_UDD_ID))
                    .prepare())
            .prepare();
    final var result = Vader.validateAndFailFast(new Bean(null, new ID("invalidId"), null), config);
    assertThat(result).contains(INVALID_UDD_ID);
  }

  // tag::bean-strict-id-validation[]
  @Test
  void idConfigForBatch() {
    final var field1EntityInfo = new Field1EntityInfo();
    final var field2EntityInfo = new Field2EntityInfo();
    final var config =
        BatchValidationConfig.<Bean, ValidationFailure>toValidate()
            .withIdConfig(
                IDConfig.<Bean, ValidationFailure, EntityInfo>toValidate()
                    .withIdValidator(BaseValidationConfigTest::uddUtil)
                    .shouldHaveValidSFIdFormatOrFailWith(
                        Tuple.of(Bean::getSfIdFormatField1, field1EntityInfo, INVALID_UDD_ID))
                    .absentOrHaveValidSFIdFormatOrFailWith(
                        Tuple.of(
                            Bean::getOptionalSfIdFormatField2,
                            field2EntityInfo,
                            INVALID_OPTIONAL_UDD_ID))
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

  /** This should be implemented by the client and passed through `withIdValidator` config. */
  private static boolean uddUtil(ID idToValidate, EntityInfo entityInfo) {
    // A core client may use `common.udd.ValidIdUtil.isThisEntity(String, EntityIdInfo)`
    return !idToValidate.toString().equalsIgnoreCase("invalidId"); // fake implementation
  }
  // end::bean-strict-id-validation[]

  @Data
  @AllArgsConstructor
  @FieldNameConstants
  // tag::bean-with-id-fields[]
  public static class Bean {
    String requiredField;
    ID sfIdFormatField1;
    ID optionalSfIdFormatField2;
  }

  /**
   * This represents `common.udd.EntityInfo` interface from core which is implemented by all
   * Entities.
   */
  private interface EntityInfo {}

  @Value
  private static class Field1EntityInfo implements EntityInfo {}

  @Value
  private static class Field2EntityInfo implements EntityInfo {}
  // end::bean-with-id-fields[]
}
