/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.execution.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.revcloud.vader.execution.Vader.validateAndFailFast;
import static org.revcloud.vader.matchers.AnyMatchers.anyOf;
import static org.revcloud.vader.matchers.IntMatchers.inRangeInclusive;
import static sample.consumer.failure.ValidationFailure.INVALID_COMBO_1;
import static sample.consumer.failure.ValidationFailure.INVALID_VALUE;

import java.util.List;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.config.ValidationConfig;
import sample.consumer.failure.ValidationFailure;

class Spec1Test {

  @DisplayName("ShouldMatch")
  @Test
  void spec1ShouldMatch() {
    final var validationConfig =
        ValidationConfig.<Bean1, ValidationFailure>toValidate()
            .withSpec(
                spec ->
                    spec._1()
                        .orFailWith(INVALID_VALUE)
                        .given(Bean1::getValue)
                        .shouldMatch(anyOf(1, 2)))
            .prepare();
    final var invalidBean = new Bean1(3);
    final var failureResult = validateAndFailFast(invalidBean, validationConfig);
    assertThat(failureResult).contains(INVALID_VALUE);

    final var validBean = new Bean1(1);
    final var noneResult = validateAndFailFast(validBean, validationConfig);
    assertThat(noneResult).isEmpty();
  }

  @Test
  @DisplayName("Validator Types from Specs")
  void validatorTypesFromSpecs() {
    final var validationConfig =
        ValidationConfig.<Bean1, ValidationFailure>toValidate()
            .withSpec(
                spec ->
                    spec._1()
                        .orFailWith(INVALID_VALUE)
                        .given(Bean1::getValue)
                        .shouldMatch(anyOf(1, 2)))
            .prepare();
    assertThat(validationConfig.getValidatableType()).isEqualTo(Bean1.class);
  }

  @DisplayName("ShouldMatchAnyOf")
  @Test
  void spec1ShouldMatchAnyOf() {
    final var validationConfig =
        ValidationConfig.<Bean1, ValidationFailure>toValidate()
            .withSpec(
                spec ->
                    spec._1()
                        .orFailWith(INVALID_VALUE)
                        .given(Bean1::getValue)
                        .shouldMatchAnyOf(List.of(anyOf(1, 2), inRangeInclusive(3, 5))))
            .prepare();
    final var invalidBean = new Bean1(6);
    final var failureResult = validateAndFailFast(invalidBean, validationConfig);
    assertThat(failureResult).contains(INVALID_VALUE);

    final var validBean = new Bean1(3);
    final var noneResult = validateAndFailFast(validBean, validationConfig);
    assertThat(noneResult).isEmpty();
  }

  @DisplayName("ShouldMatchField")
  @Test
  void spec1ShouldMatchField() {
    final var validationConfig =
        ValidationConfig.<Bean2, ValidationFailure>toValidate()
            .withSpec(
                spec ->
                    spec._1()
                        .orFailWith(INVALID_COMBO_1)
                        .given(Bean2::getField1)
                        .shouldMatchField(Bean2::getField2))
            .prepare();
    final var invalidBean = new Bean2("", null);
    final var failureResult = validateAndFailFast(invalidBean, validationConfig);
    assertThat(failureResult).contains(INVALID_COMBO_1);

    final var validBean = new Bean2("", "");
    final var noneResult = validateAndFailFast(validBean, validationConfig);
    assertThat(noneResult).isEmpty();
  }

  @DisplayName("ShouldMatchAnyOfFields")
  @Test
  void spec1ShouldMatchAnyOfFields() {
    final var validationConfig =
        ValidationConfig.<Bean3, ValidationFailure>toValidate()
            .withSpec(
                spec ->
                    spec._1()
                        .orFailWith(INVALID_COMBO_1)
                        .given(Bean3::getField1)
                        .shouldMatchAnyOfFields(List.of(Bean3::getField2, Bean3::getField3)))
            .prepare();
    final var invalidBean = new Bean3("", null, "3");
    final var failureResult = validateAndFailFast(invalidBean, validationConfig);
    assertThat(failureResult).contains(INVALID_COMBO_1);

    final var validBean = new Bean3("", null, "");
    final var noneResult = validateAndFailFast(validBean, validationConfig);
    assertThat(noneResult).isEmpty();
  }

  @Value
  private static class Bean1 {

    Integer value;
  }

  @Value
  private static class Bean2 {

    String field1;
    String field2;
  }

  @Value
  private static class Bean3 {

    String field1;
    String field2;
    String field3;
  }
}
