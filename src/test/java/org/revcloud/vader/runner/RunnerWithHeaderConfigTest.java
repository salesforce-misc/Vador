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
import static consumer.failure.ValidationFailure.MIN_BATCH_SIZE_NOT_MET;
import static consumer.failure.ValidationFailure.NONE;
import static consumer.failure.ValidationFailure.UNKNOWN_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;

class RunnerWithHeaderConfigTest {

    @Test
    void failFastForHeaderConfigWithValidators() {
        val headerConfig = HeaderValidationConfig.<HeaderBean, ValidationFailure>toValidate()
                .withBatchMapper(HeaderBean::getBatch)
                .withSimpleHeaderValidator(Tuple.of(ignore -> UNKNOWN_EXCEPTION, NONE)).prepare();
        val batch = List.of(new Bean1());
        val headerBean = new HeaderBean(batch);
        val result = Runner.validateAndFailFastForHeader(headerBean, ValidationFailure::getValidationFailureForException, headerConfig);
        assertThat(result).contains(UNKNOWN_EXCEPTION);
    }

    // TODO 29/04/21 gopala.akshintala: Write display names for tests
    @Test
    void failFastForHeaderConfigWithValidators2() {
        val headerConfig = HeaderValidationConfig.<HeaderBean, ValidationFailure>toValidate()
                .withBatchMapper(HeaderBean::getBatch)
                .withSimpleHeaderValidator(Tuple.of(ignore -> NONE, NONE)).prepare();
        val batch = List.of(new Bean1());
        val headerBean = new HeaderBean(batch);
        val result = Runner.validateAndFailFastForHeader(headerBean, ValidationFailure::getValidationFailureForException, headerConfig);
        assertThat(result).isEmpty();
    }

    @Test
    void failFastForHeaderConfigMinBatchSize() {
        val headerConfig = HeaderValidationConfig.<HeaderBean, ValidationFailure>toValidate()
                .withBatchMapper(HeaderBean::getBatch)
                .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET))
                .withSimpleHeaderValidator(Tuple.of(ignore -> NONE, NONE)).prepare();
        val headerBean = new HeaderBean(Collections.emptyList());
        val result = Runner.validateAndFailFastForHeader(headerBean, ValidationFailure::getValidationFailureForException, headerConfig);
        assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET);
    }

    @Test
    void failFastForHeaderConfigMinBatchSizeForMultiBatch() {
        val headerConfig = HeaderValidationConfig.<HeaderBeanMultiBatch, ValidationFailure>toValidate()
                .withBatchMappers(List.of(HeaderBeanMultiBatch::getBatch1, HeaderBeanMultiBatch::getBatch2))
                .shouldHaveMinBatchSize(Tuple.of(1, MIN_BATCH_SIZE_NOT_MET))
                .withSimpleHeaderValidator(Tuple.of(ignore -> NONE, NONE)).prepare();
        val headerBean = new HeaderBeanMultiBatch(Collections.emptyList(), Collections.emptyList());
        val result = Runner.validateAndFailFastForHeader(headerBean, ValidationFailure::getValidationFailureForException, headerConfig);
        assertThat(result).contains(MIN_BATCH_SIZE_NOT_MET);
    }

    @Test
    void failFastForHeaderConfigMaxBatchSize() {
        val headerConfig = HeaderValidationConfig.<HeaderBean, ValidationFailure>toValidate()
                .withBatchMapper(HeaderBean::getBatch)
                .shouldHaveMaxBatchSize(Tuple.of(0, MAX_BATCH_SIZE_EXCEEDED))
                .withSimpleHeaderValidator(Tuple.of(ignore -> NONE, NONE)).prepare();
        val headerBean = new HeaderBean(List.of(new Bean1()));
        val result = Runner.validateAndFailFastForHeader(headerBean, ValidationFailure::getValidationFailureForException, headerConfig);
        assertThat(result).contains(MAX_BATCH_SIZE_EXCEEDED);
    }

    @Test
    void headerWithFailure() {
        val validationConfig = HeaderValidationConfig.<HeaderBean, ValidationFailure>toValidate()
                .withHeaderValidators(List.of(
                        headerBean -> Either.right(NONE),
                        headerBean -> Either.left(UNKNOWN_EXCEPTION),
                        headerBean -> Either.right(NONE)))
                .withBatchMapper(HeaderBean::getBatch)
                .prepare();
        val result = Runner.validateAndFailFastForHeader(
                new HeaderBean(Collections.emptyList()),
                ValidationFailure::getValidationFailureForException,
                validationConfig
        );
        assertThat(result).contains(UNKNOWN_EXCEPTION);
    }

    @Value
    private static class HeaderBean {
        List<Bean1> batch;
    }

    @Value
    private static class HeaderBeanMultiBatch {
        List<Bean1> batch1;
        List<Bean2> batch2;
    }

    @Value
    private static class Bean1 {
    }

    @Value
    private static class Bean2 {
    }
}

