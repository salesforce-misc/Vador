package org.revcloud.vader.runner.config.nested;

import static org.assertj.core.api.Assertions.assertThat;
import static sample.consumer.failure.ValidationFailure.INVALID_UDD_ID;
import static sample.consumer.failure.ValidationFailure.INVALID_UDD_ID_3;
import static sample.consumer.failure.ValidationFailure.getFailureWithParams;

import io.vavr.Tuple;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.FieldConfig;
import org.revcloud.vader.runner.Vader;
import org.revcloud.vader.runner.ValidationConfig;
import sample.consumer.failure.ValidationFailure;



public class FieldConfigTest {
  
  @Test
  void fieldConfigWithshouldHaveValidFormatOrFailWith() {
    final var config =
      ValidationConfig.<Bean, ValidationFailure>toValidate()
        .withFieldConfig(
          FieldConfig.<String,Bean,ValidationFailure>toValidate().
            withFieldValidator(FieldConfigTest::isThisValidString).
            shouldHaveValidFormatOrFailWith(Bean::getRequiredField2,INVALID_UDD_ID))
        .withFieldConfig(
          FieldConfig.<Integer,Bean,ValidationFailure>toValidate().
            withFieldValidator(FieldConfigTest::isThisValidInteger).
            shouldHaveValidFormatOrFailWith(Bean::getRequiredField1,INVALID_UDD_ID))
        .prepare();
    final var result =
      Vader.validateAndFailFast(new Bean(100,"invalidId",null), config);
    assertThat(result).contains(INVALID_UDD_ID);
  }
  



  @Test
  void fieldConfigWithShouldHaveValidFormatForAllOrFailWithFn() {
    final var config =
      ValidationConfig.<Bean, ValidationFailure>toValidate()
        .withFieldConfig(
          FieldConfig.<String,Bean,ValidationFailure>toValidate()
            .withFieldValidator(FieldConfigTest::isThisValidString)
            .shouldHaveValidFormatForAllOrFailWithFn(
              Tuple.of( 
                List.of(Bean::getRequiredField2), 
                (invalidIdFieldName, invalidIdFieldValue) ->
                  getFailureWithParams(
                    INVALID_UDD_ID, invalidIdFieldName, invalidIdFieldValue))))
    .prepare();
    
    final var invalidString = "invalidId";
    final var result =
      Vader.validateAndFailFast(
        new Bean(null,invalidString,null), config);
    
    assertThat(result).isPresent().contains(INVALID_UDD_ID);
    assertThat(result.get().getValidationFailureMessage().getParams())
      .containsExactly("requiredField2", invalidString);
  }


  @Test
  void fieldConfigWithShouldHaveValidFormatForAllOrFailWith() {
    final var config =
      ValidationConfig.<Bean, ValidationFailure>toValidate()
        .withFieldConfig(
          FieldConfig.<String,Bean, ValidationFailure>toValidate()
            .withFieldValidator(FieldConfigTest::isThisValidString)
            .shouldHaveValidFormatForAllOrFailWith(
              Map.of(
                Bean::getRequiredField2,
                getFailureWithParams(INVALID_UDD_ID, "requiredField2"))))
        .prepare();

    final var invalidString = "invalidId";
    final var result =
      Vader.validateAndFailFast(
        new Bean(null,invalidString,null), config);
    
    assertThat(result).isPresent().contains(INVALID_UDD_ID);
    assertThat(result.get().getValidationFailureMessage().getParams()).containsExactly("requiredField2");
  }


  @Test
  void
  idConfigAbsentOrHaveValidFormatOrFailWith() {
    final var config =
      ValidationConfig.<Bean, ValidationFailure>toValidate()
        .withFieldConfig(
          FieldConfig.<String, Bean, ValidationFailure>toValidate()
            .withFieldValidator(FieldConfigTest::isThisValidString)
            .absentOrHaveValidFormatOrFailWith(
              Bean::getRequiredField2,
              getFailureWithParams(INVALID_UDD_ID_3, "requiredField2")))
        .prepare();
    final var invalidString = "invalidId";
    final var result =
      Vader.validateAndFailFast(
        new Bean(null,invalidString,null), config);
    
    assertThat(result).isPresent().contains(INVALID_UDD_ID_3);
    assertThat(result.get().getValidationFailureMessage().getParams()).containsExactly("requiredField2");
  }
  
  @Data
  @FieldNameConstants
  @AllArgsConstructor
  public static class Bean {
    private final Integer requiredField1;
    private final String requiredField2;
    private final List<String> requiredList;
  }

  private static boolean isThisValidString(String fieldToValidate) {
    return !fieldToValidate.equalsIgnoreCase("invalidId"); // fake implementation
  }
  
  private static boolean isThisValidInteger(Integer fieldToValidate) {
    return !(fieldToValidate == 100); // fake implementation
  }
  
}
