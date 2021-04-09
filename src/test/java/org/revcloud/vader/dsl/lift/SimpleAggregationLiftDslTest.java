package org.revcloud.vader.dsl.lift;

import consumer.bean.BaseParent;
import consumer.bean.Child;
import consumer.failure.ValidationFailure;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.SimpleValidator;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SimpleAggregationLiftDslTest {

    @Test
    void liftToParentValidationTypeWithNullChild() {
        SimpleValidator<Child, ValidationFailure> childValidator = child -> ValidationFailure.NONE;
        val liftedParentValidation =
                AggregationLiftSimpleDsl.liftToParentValidationType(
                        childValidator,
                        BaseParent::getChild,
                        ValidationFailure.INVALID_PARENT,
                        ValidationFailure.INVALID_CHILD
                );
        assertSame(ValidationFailure.INVALID_CHILD, liftedParentValidation.unchecked().apply(new BaseParent(0, null, null)));
    }

    @Test
    void liftToParentValidationTypeWithNullParent() {
        SimpleValidator<Child, ValidationFailure> childValidator = child -> ValidationFailure.NONE;
        val liftedParentValidation =
                AggregationLiftSimpleDsl.liftToParentValidationType(
                        childValidator,
                        BaseParent::getChild,
                        ValidationFailure.INVALID_PARENT,
                        ValidationFailure.INVALID_CHILD
                );
        assertSame(ValidationFailure.INVALID_PARENT, liftedParentValidation.unchecked().apply(null));
    }

    @Test
    void liftToParentValidationType() {
        SimpleValidator<Child, ValidationFailure> childValidator = child -> ValidationFailure.NONE;
        val liftedParentValidation =
                AggregationLiftSimpleDsl.liftToParentValidationType(
                        childValidator,
                        BaseParent::getChild,
                        ValidationFailure.INVALID_PARENT,
                        ValidationFailure.INVALID_CHILD
                );
        assertSame(ValidationFailure.NONE, liftedParentValidation.unchecked().apply(new BaseParent(0, null, new Child(0))));
    }

    @Test
    void liftToParentValidationType2ThrowForNullChild() {
        SimpleValidator<Child, ValidationFailure> childValidator = child -> {
            if (child.getId() >= 0) return ValidationFailure.NONE; // accessing some child prop to cause NPE
            return ValidationFailure.VALIDATION_FAILURE_1;
        };
        val liftedParentValidation =
                AggregationLiftSimpleDsl.liftToParentValidationType(
                        childValidator,
                        BaseParent::getChild,
                        ValidationFailure.INVALID_PARENT
                );
        final BaseParent parentWithNullChild = new BaseParent(0, null, null);
        assertThrows(NullPointerException.class, () -> liftedParentValidation.apply(parentWithNullChild));
    }

    @Test
    void liftToParentValidationType2NullParent() {
        SimpleValidator<Child, ValidationFailure> childValidator = child -> {
            if (child.getId() >= 0) return ValidationFailure.NONE; // accessing some child prop to cause NPE
            return ValidationFailure.VALIDATION_FAILURE_1;
        };
        val liftedParentValidation =
                AggregationLiftSimpleDsl.liftToParentValidationType(
                        childValidator,
                        BaseParent::getChild,
                        ValidationFailure.INVALID_PARENT
                );
        assertSame(ValidationFailure.INVALID_PARENT,liftedParentValidation.unchecked().apply(null));
    }
    
    @Test
    void liftToParentValidationType2ForFailure() {
        SimpleValidator<Child, ValidationFailure> childValidator = child -> {
            if (child.getId() >= 0) return ValidationFailure.NONE; // accessing some child prop to cause NPE
            return ValidationFailure.VALIDATION_FAILURE_1;
        };
        val liftedParentValidation =
                AggregationLiftSimpleDsl.liftToParentValidationType(
                        childValidator,
                        BaseParent::getChild,
                        ValidationFailure.INVALID_PARENT
                );
        val validatable = new BaseParent(0, null, new Child(-1));
        assertSame(ValidationFailure.VALIDATION_FAILURE_1,liftedParentValidation.unchecked().apply(validatable));
    }

}
