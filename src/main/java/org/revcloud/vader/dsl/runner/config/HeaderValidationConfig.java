package org.revcloud.vader.dsl.runner.config;

import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Getter;

import java.util.List;

@Getter
public final class HeaderValidationConfig<ValidatableT, FailureT> {
    private Tuple2<Integer, FailureT> minBatchSize = Tuple.of(Integer.MIN_VALUE, null);
    private Tuple2<Integer, FailureT> maxBatchSize = Tuple.of(Integer.MAX_VALUE, null);
    private Function1<ValidatableT, List<?>> batchMapper = null; 
    
    private HeaderValidationConfig() {
    }

    @SuppressWarnings("unused")
    public static <ValidatableT, FailureT> HeaderValidationConfig<ValidatableT, FailureT> toValidate(Class<ValidatableT> ignore1, Class<FailureT> ignore2) {
        return new HeaderValidationConfig<>();
    }

    public final HeaderValidationConfig<ValidatableT, FailureT> withMinBatchSize(int minBatchSize, FailureT failureT) {
        this.minBatchSize = Tuple.of(minBatchSize, failureT);
        return this;
    }

    public final HeaderValidationConfig<ValidatableT, FailureT> withMaxBatchSize(int maxBatchSize, FailureT failureT) {
        this.maxBatchSize = Tuple.of(maxBatchSize, failureT);
        return this;
    }

    public final HeaderValidationConfig<ValidatableT, FailureT> withBatchMapper(Function1<ValidatableT, List<?>> batchMapper) {
        this.batchMapper = batchMapper;
        return this;
    }
}
