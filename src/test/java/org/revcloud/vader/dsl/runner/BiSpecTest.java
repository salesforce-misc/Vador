package org.revcloud.vader.dsl.runner;

import consumer.failure.ValidationFailure;
import io.vavr.Function1;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static consumer.failure.ValidationFailure.INVALID_COMBO;
import static consumer.failure.ValidationFailure.NONE;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BiSpecTest {
    @Test
    void biSpecTest() {
        val biSpec = BiSpec.<Bean, ValidationFailure>check().orFailWith(INVALID_COMBO)
                .when(Bean::getValue).is(1)
                .then(Bean::getValueStr).shouldBe(either(is("one")).or(is("1"))).done();
        assertFalse(biSpec.toPredicate().test(new Bean(1,"two")));
        // TODO 15/04/21 gopala.akshintala: Assert on `orFailWith` instead of true/false 
    }
    
    @Value
    static class Bean {
        Integer value;
        String valueStr;
    }
}


