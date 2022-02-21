package org.revcloud.vader.runner.config.nested;

import static org.assertj.core.api.Assertions.assertThat;
import static sample.consumer.failure.ValidationFailure.INVALID_UDD_ID;

import java.util.List;
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
  void fieldConfig() {
    final var config =
      ValidationConfig.<Bean, ValidationFailure>toValidate()
        .withFieldConfig(
          FieldConfig.<String,Bean,ValidationFailure>toValidate().
            withFieldValidator(FieldConfigTest::isThisValidString).
            shouldHaveValidFormatOrFailWith(Bean::getRequiredField2,INVALID_UDD_ID)
        ).prepare();
    final var result =
      Vader.validateAndFailFast(new Bean(null,"invalidId",null), config);
    assertThat(result).contains(INVALID_UDD_ID);
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
  
}
