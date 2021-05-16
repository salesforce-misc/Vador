package org.revcloud.vader.lift;

import static consumer.failure.ValidationFailure.NONE;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.revcloud.vader.lift.AggregationLiftSimpleUtil.liftAllToContainerValidatorType;
import static org.revcloud.vader.lift.AggregationLiftSimpleUtil.liftToContainerValidatorType;

import consumer.bean.Container;
import consumer.bean.Member;
import consumer.failure.ValidationFailure;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.SimpleValidator;

class AggregationLiftSimpleUtilTest {

  @DisplayName("Lifts proper Member validation")
  @Test
  void liftToContainerValidationType() {
    SimpleValidator<Member, ValidationFailure> memberValidator = member -> NONE;
    final var liftedContainerValidation =
        liftToContainerValidatorType(memberValidator, Container::getMember);
    assertSame(NONE, liftedContainerValidation.unchecked().apply(new Container(0, new Member(0))));
  }

  @DisplayName("Lifted Member validation does NOT deal with Null Member")
  @Test
  void liftToContainerValidationType2ThrowForNullMember() {
    SimpleValidator<Member, ValidationFailure> memberValidator =
        member -> {
          if (member.getId() >= 0) return NONE; // accessing some member prop to cause NPE
          return ValidationFailure.VALIDATION_FAILURE_1;
        };
    final var liftedContainerValidation =
        liftToContainerValidatorType(memberValidator, Container::getMember);
    final var containerWithNullMember = new Container(0, null);
    assertThrows(
        NullPointerException.class, () -> liftedContainerValidation.apply(containerWithNullMember));
  }

  @DisplayName("Lifted Member validation does NOT deal with Null Container")
  @Test
  void liftToContainerValidationType2NullContainer() {
    SimpleValidator<Member, ValidationFailure> memberValidator = member -> null;
    final var liftedContainerValidation =
        liftToContainerValidatorType(memberValidator, Container::getMember);
    assertThrows(NullPointerException.class, () -> liftedContainerValidation.apply(null));
  }

  @DisplayName("Lift when Member validation is Null")
  @Test
  void liftNullToContainerValidationType() {
    SimpleValidator<Member, ValidationFailure> memberValidator = null;
    assertThrows(
        NullPointerException.class,
        () -> liftToContainerValidatorType(memberValidator, Container::getMember));
  }

  @DisplayName("Lift All proper Member validations")
  @Test
  void liftAllToContainerValidationType() {
    List<SimpleValidator<Member, ValidationFailure>> memberValidators =
        List.of(
            member -> {
              if (member.getId() >= 0) return NONE; // accessing some member prop to cause NPE
              return ValidationFailure.VALIDATION_FAILURE_1;
            },
            member -> {
              if (member.getId() >= 0) return NONE; // accessing some member prop to cause NPE
              return ValidationFailure.VALIDATION_FAILURE_1;
            },
            member -> {
              if (member.getId() >= 0) return NONE; // accessing some member prop to cause NPE
              return ValidationFailure.VALIDATION_FAILURE_1;
            });
    final var liftedContainerValidations =
        liftAllToContainerValidatorType(memberValidators, Container::getMember);
    final var validContainer = new Container(0, new Member(1));
    assertTrue(
        liftedContainerValidations.stream()
            .allMatch(v -> v.unchecked().apply(validContainer) == NONE));
  }
}
