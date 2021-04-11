package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.revcloud.vader.types.validators.SimpleValidator;

@Slf4j
@UtilityClass
class FailFastSimple {

    /**
     * Higher-order function to compose list of Simple validators into Fail-Fast Strategy.
     *
     * @param validators
     * @param invalidValidatable
     * @param none               Value to be returned in case of no failure.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Composed Fail-Fast Strategy
     */
    static <FailureT, ValidatableT> SimpleFailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper) {
        return validatable -> validatable == null
                ? invalidValidatable
                : Utils.applySimpleValidators(validatable, validators.iterator(), throwableMapper)
                .filter(result -> result != none).getOrElse(none);
    }

    static <FailureT, ValidatableT> SimpleFailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatable -> validatable == null
                ? invalidValidatable
                : Utils.applySimpleValidators(validatable, Iterator.concat(Utils.toSimpleValidators(validationConfig, none), validators), throwableMapper)
                .filter(result -> result != none).getOrElse(none);
    }

    static <FailureT, ValidatableT> SimpleFailFastStrategy<ValidatableT, FailureT> failFastStrategyForHeader(
            List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper,
            HeaderValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatable -> {
            if (validatable == null) {
                return invalidValidatable;
            }
            val batch = validationConfig.getBatchMapper().apply(validatable);
            val batchSizeFailure = Utils.validateSize(batch, none, validationConfig);
            if (batchSizeFailure != none) {
                return batchSizeFailure;
            }
            return Utils.applySimpleValidators(validatable, validators.iterator(), throwableMapper)
                    .filter(result -> result != none).getOrElse(none);
        };
    }

    @FunctionalInterface
    interface SimpleFailFastStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, FailureT> {
    }
}
