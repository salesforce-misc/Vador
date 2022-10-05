/*******************************************************************************
 * Copyright (c) 2022, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 ******************************************************************************/

package com.salesforce.vador.lift;

import static com.salesforce.vador.lift.AggregationLiftUtil.liftAllToContainerValidatorType;
import static com.salesforce.vador.lift.AggregationLiftUtil.liftToContainerValidatorType;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sample.consumer.failure.ValidationFailure.NONE;

import com.salesforce.vador.types.Validator;
import java.util.List;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sample.consumer.failure.ValidationFailure;

class AggregationLiftUtilTest {

  @DisplayName("Lifts proper Member validation")
  @Test
  void liftToContainerValidationType() {
    Validator<Member, ValidationFailure> memberValidator = member -> NONE;
    final var liftedContainerValidation =
        liftToContainerValidatorType(memberValidator, Container::getMember);
    assertSame(NONE, liftedContainerValidation.unchecked().apply(new Container(new Member(0))));
  }

  @DisplayName("Lifted Member validation does NOT deal with Null Member")
  @Test
  void liftToContainerValidationType2ThrowForNullMember() {
    Validator<Member, ValidationFailure> memberValidator =
        member -> {
          if (member.getId() >= 0) {
            return NONE; // accessing some member prop to cause NPE
          }
          return ValidationFailure.VALIDATION_FAILURE_1;
        };
    final var liftedContainerValidation =
        liftToContainerValidatorType(memberValidator, Container::getMember);
    final var containerWithNullMember = new Container(null);
    assertThrows(
        NullPointerException.class, () -> liftedContainerValidation.apply(containerWithNullMember));
  }

  @DisplayName("Lifted Member validation does NOT deal with Null Container")
  @Test
  void liftToContainerValidationType2NullContainer() {
    Validator<Member, ValidationFailure> memberValidator = member -> null;
    final var liftedContainerValidation =
        liftToContainerValidatorType(memberValidator, Container::getMember);
    assertThrows(NullPointerException.class, () -> liftedContainerValidation.apply(null));
  }

  @DisplayName("Lift when Member validation is Null")
  @Test
  void liftNullToContainerValidationType() {
    Validator<Member, ValidationFailure> memberValidator = null;
    assertThrows(
        NullPointerException.class,
        () -> liftToContainerValidatorType(memberValidator, Container::getMember));
  }

  @DisplayName("Lift All proper Member validations")
  @Test
  void liftAllToContainerValidationType() {
    List<Validator<Member, ValidationFailure>> memberValidators =
        List.of(
            member -> {
              if (member.getId() >= 0) {
                return NONE; // accessing a member prop to cause NPE
              }
              return ValidationFailure.VALIDATION_FAILURE_1;
            },
            member -> {
              if (member.getId() >= 0) {
                return NONE; // accessing a member prop to cause NPE
              }
              return ValidationFailure.VALIDATION_FAILURE_1;
            },
            member -> {
              if (member.getId() >= 0) {
                return NONE; // accessing a member prop to cause NPE
              }
              return ValidationFailure.VALIDATION_FAILURE_1;
            });
    final var liftedContainerValidations =
        liftAllToContainerValidatorType(memberValidators, Container::getMember);
    final var validContainer = new Container(new Member(1));
    assertTrue(
        liftedContainerValidations.stream()
            .allMatch(v -> v.unchecked().apply(validContainer) == NONE));
  }

  @Value
  private static class Container {
    Member member;
  }

  @Value
  private static class Member {
    int id;
  }
}
