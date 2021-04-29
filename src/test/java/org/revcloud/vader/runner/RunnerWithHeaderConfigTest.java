package org.revcloud.vader.runner;

import consumer.failure.ValidationFailure;
import io.vavr.Tuple;
import io.vavr.control.Either;
import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static consumer.failure.ValidationFailure.MAX_BATCH_SIZE_EXCEEDED;
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_EXCEEDED;
import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.NOTHING_TO_VALIDATE;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;

class RunnerWithHeaderConfigTest {

    @Test
    void failFastForHeaderConfigWithValidators() {
        val headerConfig = HeaderValidationConfig.<HeaderBean, ValidationFailure>toValidate()
                .withBatchMapper(HeaderBean::getBeans)
                .withSimpleValidator(Tuple.of(ignore -> UNKNOWN_EXCEPTION, NONE)).prepare();
        val beans = Collections.<Bean>emptyList();
        val headerBean = new HeaderBean(beans);
        val result = Runner.validateAndFailFastForHeader(headerBean, NOTHING_TO_VALIDATE, ValidationFailure::getValidationFailureForException, headerConfig);
        assertThat(result).contains(UNKNOWN_EXCEPTION);
    }

    @Test
    void failFastForHeaderConfigWithValidators2() {
        val headerConfig = HeaderValidationConfig.<HeaderBean, ValidationFailure>toValidate()
                .withBatchMapper(HeaderBean::getBeans)
                .withSimpleValidator(Tuple.of(ignore -> NONE, NONE)).prepare();
        val beans = Collections.<Bean>emptyList();
        val headerBean = new HeaderBean(beans);
        val result = Runner.validateAndFailFastForHeader(headerBean, NOTHING_TO_VALIDATE, ValidationFailure::getValidationFailureForException, headerConfig);
        assertThat(result).isEmpty();
    }

    @Test
    void failFastForHeaderConfigMinBatchSize() {
        val headerConfig = HeaderValidationConfig.<HeaderBean, ValidationFailure>toValidate()
                .withBatchMapper(HeaderBean::getBeans)
                .minBatchSize(Tuple.of(1, MIN_BATCH_SIZE_EXCEEDED))
                .withSimpleValidator(Tuple.of(ignore -> NONE, NONE)).prepare();
        val beans = Collections.<Bean>emptyList();
        val headerBean = new HeaderBean(beans);
        val result = Runner.validateAndFailFastForHeader(headerBean, NOTHING_TO_VALIDATE, ValidationFailure::getValidationFailureForException, headerConfig);
        assertThat(result).contains(MIN_BATCH_SIZE_EXCEEDED);
    }

    @Test
    void failFastForHeaderConfigMaxBatchSize() {
        val headerConfig = HeaderValidationConfig.<HeaderBean, ValidationFailure>toValidate()
                .withBatchMapper(HeaderBean::getBeans)
                .maxBatchSize(Tuple.of(0, MAX_BATCH_SIZE_EXCEEDED))
                .withSimpleValidator(Tuple.of(ignore -> NONE, NONE)).prepare();
        val beans = List.of(new Bean());
        val headerBean = new HeaderBean(beans);
        val result = Runner.validateAndFailFastForHeader(headerBean, NOTHING_TO_VALIDATE, ValidationFailure::getValidationFailureForException, headerConfig);
        assertThat(result).contains(MAX_BATCH_SIZE_EXCEEDED);
    }

    @Test
    void headerWithFailure() {
        val validationConfig = HeaderValidationConfig.<HeaderBean, ValidationFailure>toValidate()
                .withValidators(List.of(
                        headerBean -> Either.right(NONE),
                        headerBean -> Either.left(UNKNOWN_EXCEPTION),
                        headerBean -> Either.right(NONE)))
                .withBatchMapper(HeaderBean::getBeans)
                .prepare();
        val result = Runner.validateAndFailFastForHeader(
                new HeaderBean(Collections.emptyList()),
                NOTHING_TO_VALIDATE,
                ValidationFailure::getValidationFailureForException,
                validationConfig
        );
        assertThat(result).contains(UNKNOWN_EXCEPTION);
    }

    @Value
    private static class HeaderBean {
        List<Bean> beans;
    }

    @Value
    private static class Bean {
    }
}

