package org.qtc.delphinus;

import consumer.failure.ValidationFailure;
import consumer.representation.Parent;
import io.vavr.collection.List;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.qtc.delphinus.types.validators.simple.SimpleValidator;

class StrategiesTest {

    @Test
    void simpleValidationsFailFastStrategy() {
        SimpleValidator<Parent, ValidationFailure> v1 = parent -> ValidationFailure.SUCCESS; 
        SimpleValidator<Parent, ValidationFailure> v2 = parent -> ValidationFailure.SUCCESS; 
        SimpleValidator<Parent, ValidationFailure> v3 = parent -> ValidationFailure.NOTHING_TO_VALIDATE;
        
        val validationList = List.of(v1, v2, v3);
        val result = 
                Strategies.failFastStrategy(validationList, ValidationFailure.NOTHING_TO_VALIDATE, ValidationFailure.SUCCESS).apply(new Parent());
        Assertions.assertSame(ValidationFailure.NOTHING_TO_VALIDATE, result);
    }
}
