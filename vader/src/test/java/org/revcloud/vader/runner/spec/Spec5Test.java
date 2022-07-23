/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package org.revcloud.vader.runner.spec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static sample.consumer.failure.ValidationFailure.INVALID_BEAN;
import static sample.consumer.failure.ValidationFailure.INVALID_BEAN_1;
import static sample.consumer.failure.ValidationFailure.INVALID_BEAN_2;

import io.vavr.Tuple;
import java.util.List;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.Vader;
import org.revcloud.vader.runner.ValidationConfig;
import sample.consumer.failure.ValidationFailure;

class Spec5Test {
  @Test
  void spec5TestWithValidBean() {
    final var config =
        ValidationConfig.<Bean1, ValidationFailure>toValidate()
            .withSpec(
                spec ->
                    spec._5()
                        .whenAllTheseFieldsMatch(
                            Tuple.of(
                                List.of(
                                    Bean1::getWhenField1,
                                    Bean1::getWhenField2,
                                    Bean1::getWhenField3),
                                notNullValue()))
                        .thenAllThoseFieldsShouldMatch(
                            Tuple.of(
                                List.of(
                                    Bean1::getThenField1,
                                    Bean1::getThenField2,
                                    Bean1::getThenField3),
                                nullValue()))
                        .orFailWith(INVALID_BEAN))
            .prepare();

    final var validBean = new Bean1(1, "2", new Field(3), null, null, null);
    final var result = Vader.validateAndFailFast(validBean, config);
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("One Then field doesn't match")
  void spec5TestWithInValidBean() {
    final var config =
        ValidationConfig.<Bean1, ValidationFailure>toValidate()
            .withSpec(
                spec ->
                    spec._5()
                        .whenAllTheseFieldsMatch(
                            Tuple.of(
                                List.of(
                                    Bean1::getWhenField1,
                                    Bean1::getWhenField2,
                                    Bean1::getWhenField3),
                                notNullValue()))
                        .thenAllThoseFieldsShouldMatch(
                            Tuple.of(
                                List.of(
                                    Bean1::getThenField1,
                                    Bean1::getThenField2,
                                    Bean1::getThenField3),
                                nullValue()))
                        .orFailWith(INVALID_BEAN))
            .prepare();

    final var validBean = new Bean1(1, "2", new Field(3), null, "doesn't match", null);
    final var result = Vader.validateAndFailFast(validBean, config);
    assertThat(result).contains(INVALID_BEAN);
  }

  @Test
  @DisplayName("One When field doesn't match criteria")
  void spec5TestBeanDoesNotMeetWhenCriteria() {
    final var config =
        ValidationConfig.<Bean1, ValidationFailure>toValidate()
            .withSpec(
                spec ->
                    spec._5()
                        .whenAllTheseFieldsMatch(
                            Tuple.of(
                                List.of(
                                    Bean1::getWhenField1,
                                    Bean1::getWhenField2,
                                    Bean1::getWhenField3),
                                notNullValue()))
                        .thenAllThoseFieldsShouldMatch(
                            Tuple.of(
                                List.of(
                                    Bean1::getThenField1,
                                    Bean1::getThenField2,
                                    Bean1::getThenField3),
                                nullValue()))
                        .orFailWith(INVALID_BEAN))
            .prepare();

    final var validBean = new Bean1(1, null, new Field(3), null, "doesn't match", null);
    final var result = Vader.validateAndFailFast(validBean, config);
    assertThat(result).isEmpty();
  }

  @Test
  void spec5TestInvalidBeanWithMultiSpec5() {
    final var config =
        ValidationConfig.<Bean2, ValidationFailure>toValidate()
            .specify(
                spec ->
                    List.of(
                        spec._5()
                            .whenAllTheseFieldsMatch(
                                Tuple.of(
                                    List.of(Bean2::getWhenField1, Bean2::getWhenField2),
                                    equalTo(1)))
                            .thenAllThoseFieldsShouldMatch(
                                Tuple.of(
                                    List.of(Bean2::getThenField1, Bean2::getThenField2),
                                    equalTo("1")))
                            .orFailWith(INVALID_BEAN_1),
                        spec._5()
                            .whenAllTheseFieldsMatch(
                                Tuple.of(
                                    List.of(Bean2::getWhenField1, Bean2::getWhenField2),
                                    equalTo(1)))
                            .thenAllThoseFieldsShouldMatch(
                                Tuple.of(
                                    List.of(Bean2::getThenField1, Bean2::getThenField2),
                                    nullValue()))
                            .orFailWith(INVALID_BEAN_2)))
            .prepare();
    final var invalidBean = new Bean2(1, 1, "1", "1");
    final var result = Vader.validateAndFailFast(invalidBean, config);
    assertThat(result).contains(INVALID_BEAN_2);
  }

  @Value
  private static class Bean1 {
    int whenField1;
    String whenField2;
    Field whenField3;

    Integer thenField1;
    String thenField2;
    Field thenField3;
  }

  @Value
  private static class Bean2 {
    int whenField1;
    int whenField2;

    String thenField1;
    String thenField2;
  }

  @Value
  private static class Field {
    int id;
  }
}
