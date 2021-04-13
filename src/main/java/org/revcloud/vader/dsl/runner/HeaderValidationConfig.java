package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class HeaderValidationConfig<ValidatableT, FailureT> {
    @Builder.Default
    Tuple2<Integer, FailureT> minBatchSize = Tuple.of(Integer.MIN_VALUE, null);
    @Builder.Default
    Tuple2<Integer, FailureT> maxBatchSize = Tuple.of(Integer.MAX_VALUE, null);
    Function1<ValidatableT, List<?>> withBatchMapper;
}
