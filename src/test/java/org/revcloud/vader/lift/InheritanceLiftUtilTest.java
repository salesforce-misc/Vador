package org.revcloud.vader.lift;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.revcloud.vader.runner.ValidationConfig;
import org.revcloud.vader.types.validators.SimpleValidator;

import java.util.List;

import static consumer.failure.ValidationFailure.NONE;

class InheritanceLiftUtilTest {
    @Test
    void liftToChildValidatorTypeTest() {
        final SimpleValidator<Bean1, ValidationFailure> v1 = ignore -> NONE; 
        final SimpleValidator<Bean2, ValidationFailure> v2 = ignore -> NONE;
        final SimpleValidator<Bean3, ValidationFailure> v3 = ignore -> NONE;
        //ValidationConfig.<Bean2, ValidationFailure>toValidate().withSimpleValidators(Tuple.of(List.of(v1, v2), NONE));
    }
    
    private abstract class Bean1 {
        
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    private class Bean2 extends Bean1 {
        
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    private class Bean3 extends Bean2 {

    }
}
