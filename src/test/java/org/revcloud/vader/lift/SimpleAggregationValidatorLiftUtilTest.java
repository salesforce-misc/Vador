package org.revcloud.vader.lift;

import consumer.bean.Container;
import consumer.bean.Member;
import consumer.failure.ValidationFailure;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.SimpleValidator;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimpleAggregationValidatorLiftUtilTest {

    @Test
    void liftToContainerValidationTypeWithNullMember() {
        SimpleValidator<Member, ValidationFailure> childValidator = child -> ValidationFailure.NONE;
        val liftedContainerValidation =
                AggregationLiftSimpleUtil.liftToContainerValidatorType(
                        childValidator,
                        Container::getMember,
                        ValidationFailure.INVALID_PARENT,
                        ValidationFailure.INVALID_CHILD
                );
        assertSame(ValidationFailure.INVALID_CHILD, liftedContainerValidation.unchecked().apply(new Container(0, null)));
    }

    @Test
    void liftToContainerValidationTypeWithNullContainer() {
        SimpleValidator<Member, ValidationFailure> childValidator = child -> ValidationFailure.NONE;
        val liftedContainerValidation =
                AggregationLiftSimpleUtil.liftToContainerValidatorType(
                        childValidator,
                        Container::getMember,
                        ValidationFailure.INVALID_PARENT,
                        ValidationFailure.INVALID_CHILD
                );
        assertSame(ValidationFailure.INVALID_PARENT, liftedContainerValidation.unchecked().apply(null));
    }

    @Test
    void liftToContainerValidationType() {
        SimpleValidator<Member, ValidationFailure> childValidator = child -> ValidationFailure.NONE;
        val liftedContainerValidation =
                AggregationLiftSimpleUtil.liftToContainerValidatorType(
                        childValidator,
                        Container::getMember,
                        ValidationFailure.INVALID_PARENT,
                        ValidationFailure.INVALID_CHILD
                );
        assertSame(ValidationFailure.NONE, liftedContainerValidation.unchecked().apply(new Container(0, new Member(0))));
    }

    @Test
    void liftToContainerValidationType2ThrowForNullMember() {
        SimpleValidator<Member, ValidationFailure> childValidator = child -> {
            if (child.getId() >= 0) return ValidationFailure.NONE; // accessing some child prop to cause NPE
            return ValidationFailure.VALIDATION_FAILURE_1;
        };
        val liftedContainerValidation =
                AggregationLiftSimpleUtil.liftToContainerValidatorType(
                        childValidator,
                        Container::getMember,
                        ValidationFailure.INVALID_PARENT
                );
        final Container parentWithNullMember = new Container(0, null);
        assertThrows(NullPointerException.class, () -> liftedContainerValidation.apply(parentWithNullMember));
    }

    @Test
    void liftToContainerValidationType2NullContainer() {
        SimpleValidator<Member, ValidationFailure> childValidator = child -> {
            if (child.getId() >= 0) return ValidationFailure.NONE; // accessing some child prop to cause NPE
            return ValidationFailure.VALIDATION_FAILURE_1;
        };
        val liftedContainerValidation =
                AggregationLiftSimpleUtil.liftToContainerValidatorType(
                        childValidator,
                        Container::getMember,
                        ValidationFailure.INVALID_PARENT
                );
        assertSame(ValidationFailure.INVALID_PARENT,liftedContainerValidation.unchecked().apply(null));
    }
    
    @Test
    void liftToContainerValidationType2ForFailure() {
        SimpleValidator<Member, ValidationFailure> childValidator = child -> {
            if (child.getId() >= 0) return ValidationFailure.NONE; // accessing some child prop to cause NPE
            return ValidationFailure.VALIDATION_FAILURE_1;
        };
        val liftedContainerValidation =
                AggregationLiftSimpleUtil.liftToContainerValidatorType(
                        childValidator,
                        Container::getMember,
                        ValidationFailure.INVALID_PARENT
                );
        val validatable = new Container(0, new Member(-1));
        assertSame(ValidationFailure.VALIDATION_FAILURE_1,liftedContainerValidation.unchecked().apply(validatable));
    }

    // TODO 13/04/21 gopala.akshintala: Have bean specific to test 
}
