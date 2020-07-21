package org.qtc.delphinus.dsl;

import consumer.failure.ValidationFailure;
import consumer.bean.Child;
import consumer.bean.Parent;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.qtc.delphinus.types.validators.simple.SimpleValidator;

import static org.junit.jupiter.api.Assertions.assertSame;

class SimpleParentChildDslTest {

    @Test
    void liftToParentValidationTypeWithNullChild() {
        SimpleValidator<Child, ValidationFailure> childValidator = child -> ValidationFailure.NONE;
        val liftedParentValidation =
                SimpleParentChildDsl.liftToParentValidationType(
                        childValidator, 
                        Parent::getChild, 
                        ValidationFailure.INVALID_PARENT, 
                        ValidationFailure.INVALID_CHILD
                );
        assertSame(ValidationFailure.INVALID_CHILD, liftedParentValidation.apply(new Parent(null)));
    }

    @Test
    void liftToParentValidationTypeWithNullParent() {
        SimpleValidator<Child, ValidationFailure> childValidator = child -> ValidationFailure.NONE;
        val liftedParentValidation =
                SimpleParentChildDsl.liftToParentValidationType(
                        childValidator,
                        Parent::getChild,
                        ValidationFailure.INVALID_PARENT,
                        ValidationFailure.INVALID_CHILD
                );
        assertSame(ValidationFailure.INVALID_PARENT, liftedParentValidation.apply(null));
    }
    
}
