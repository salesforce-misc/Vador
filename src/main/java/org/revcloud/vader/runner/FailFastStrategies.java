package org.revcloud.vader.runner;

import io.vavr.Function1;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.revcloud.vader.runner.Utils.filterInvalidatablesAndDuplicates;
import static org.revcloud.vader.runner.Utils.findFirstFailure;

@UtilityClass
class FailFastStrategies {

    /**
     * Config
     *
     * @param invalidValidatable
     * @param throwableMapper
     * @param validationConfig
     * @param <FailureT>
     * @param <ValidatableT>
     * @return
     */
    static <FailureT, ValidatableT> FailFast<ValidatableT, FailureT> failFast(
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatable -> findFirstFailure(Either.right(validatable), validationConfig, throwableMapper);
    }

    /**
     * Batch + Simple + Config
     *
     * @param invalidValidatable
     * @param throwableMapper
     * @param validationConfig
     * @param <FailureT>
     * @param <ValidatableT>
     * @return
     */
    static <FailureT, ValidatableT> FailFastForBatch<ValidatableT, FailureT> failFastForBatch(
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            BatchValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatables -> {
            val filteredValidatables = filterInvalidatablesAndDuplicates(validatables, invalidValidatable, validationConfig);
            return filteredValidatables
                    .map(validatable -> findFirstFailure(validatable, validationConfig, throwableMapper)).toJavaList();
        };
    }

    static <FailureT, ValidatableT> FailFastAllOrNoneForBatch<ValidatableT, FailureT> failFastAllOrNoneForBatch(
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            BatchValidationConfig<ValidatableT, FailureT> batchValidationConfig) {
        return validatables -> Utils.filterInvalidatablesAndDuplicatesForAllOrNone(validatables, invalidValidatable, batchValidationConfig)
                .or(() -> validatables.stream().map(Either::<FailureT, ValidatableT>right)
                        .map(validatable -> findFirstFailure(validatable, batchValidationConfig, throwableMapper))
                        .filter(Either::isLeft).findFirst().map(Either::getLeft));
    }

    static <FailureT, ValidatableT> FailFastStrategyForHeader<ValidatableT, FailureT> failFastForHeader(
            Function1<Throwable, FailureT> throwableMapper,
            HeaderValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatable -> {
            val batch = validationConfig.getWithBatchMappers().stream()
                    .map(mapper -> mapper.get(validatable))
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream).collect(Collectors.toList());
            return Utils.validateSize(batch, validationConfig).or(() ->
                    Utils.fireValidators(Either.right(validatable), validationConfig.getHeaderValidatorsStream(), throwableMapper)
                            .filter(Either::isLeft).findFirst()
                            .map(Either::swap).flatMap(Either::toJavaOptional));
        };
    }

    @FunctionalInterface
    interface FailFast<ValidatableT, FailureT> extends Function1<ValidatableT, Either<FailureT, ValidatableT>> {
    }

    @FunctionalInterface
    interface FailFastForBatch<ValidatableT, FailureT> extends Function1<List<ValidatableT>, List<Either<FailureT, ValidatableT>>> {
    }

    @FunctionalInterface
    interface FailFastAllOrNoneForBatch<ValidatableT, FailureT> extends Function1<List<ValidatableT>, Optional<FailureT>> {
    }

    @FunctionalInterface
    interface FailFastStrategyForHeader<ValidatableT, FailureT> extends Function1<ValidatableT, Optional<FailureT>> {
    }
}

