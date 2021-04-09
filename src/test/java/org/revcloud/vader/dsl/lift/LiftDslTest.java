package org.revcloud.vader.dsl.lift;

import consumer.bean.BaseParent;
import consumer.failure.ValidationFailure;
import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.types.validators.SimpleValidator;

class LiftDslTest {

    @Test
    void liftSimpleForFailure() {
        SimpleValidator<BaseParent, ValidationFailure> simpleValidator = parent -> ValidationFailure.VALIDATION_FAILURE_1;
        val liftedValidator = LiftDsl.liftSimple(simpleValidator, ValidationFailure.NONE);
        Assertions.assertEquals(liftedValidator.unchecked().apply(Either.right(new BaseParent(0, null, null))),
                Either.left(ValidationFailure.VALIDATION_FAILURE_1));
    }

    @Test
    void liftSimpleForNoFailure() {
        SimpleValidator<BaseParent, ValidationFailure> simpleValidator = parent -> ValidationFailure.NONE;
        val liftedValidator = LiftDsl.liftSimple(simpleValidator, ValidationFailure.NONE);
        final BaseParent toBeValidated = new BaseParent(0, null, null);
        Assertions.assertEquals(liftedValidator.unchecked().apply(Either.right(toBeValidated)),
                Either.right(toBeValidated));
    }
}
