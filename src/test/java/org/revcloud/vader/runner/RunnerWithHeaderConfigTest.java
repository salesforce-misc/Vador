package org.revcloud.vader.runner;

import consumer.failure.ValidationFailure;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.control.Option;
import lombok.Value;
import lombok.val;
import org.assertj.vavr.api.VavrAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_EXCEEDED;
import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;

class RunnerWithHeaderConfigTest {

    private static <ValidatableT, FailureT> Option<FailureT> validateAndFailFastForHeader(
            ValidatableT validatable,
            FailureT nothingToValidate,
            Function1<Throwable, FailureT> throwableMapper,
            HeaderValidationConfig<ValidatableT, FailureT> headerValidationConfig) {
        return Runner.validateAndFailFastForHeader(
                validatable,
                nothingToValidate,
                throwableMapper,
                headerValidationConfig);
    }

    @Test
    void failFastForHeaderConfig() {
        val headerConfig = HeaderValidationConfig.<HeaderBean, ValidationFailure>toValidate()
                .withBatchMapper(HeaderBean::getBeans)
                .minBatchSize(Tuple.of(1, MIN_BATCH_SIZE_EXCEEDED))
                .withSimpleValidator(Tuple.of(ignore -> NONE, NONE)).prepare();
        val beans = List.<Bean>of();
        val headerBean = new HeaderBean(beans);
        val result = validateAndFailFastForHeader(headerBean, NOTHING_TO_VALIDATE, ignore -> UNKNOWN_EXCEPTION, headerConfig);
        VavrAssertions.assertThat(result).contains(MIN_BATCH_SIZE_EXCEEDED);
    }

    @Value
    private static class HeaderBean {
        List<Bean> beans;
    }

    @Value
    private static class Bean {

    }
}

