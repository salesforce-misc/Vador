package org.revcloud.vader.runner;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.revcloud.vader.types.validators.SimpleValidator;

import java.util.stream.Stream;

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
                : Utils.fireSimpleValidators(validatable, validators.toJavaStream(), throwableMapper)
                .filter(result -> result != none).findFirst().orElse(none);
    }

    static <FailureT, ValidatableT> SimpleFailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatable -> validatable == null
                ? invalidValidatable
                : Utils.fireSimpleValidators(validatable, Stream.concat(Utils.toSimpleValidators(validationConfig, none), validators.toJavaStream()), throwableMapper)
                .filter(result -> result != none).findFirst().orElse(none);
    }

    static <FailureT, ValidatableT> SimpleFailFastStrategyForHeader<ValidatableT, FailureT> failFastStrategyForHeader(
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            HeaderValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatable -> {
            if (validatable == null) {
                return Option.of(invalidValidatable);
            }
            val batch = validationConfig.getWithBatchMapper().apply(validatable);
            val batchSizeFailure = Utils.validateSize(batch, validationConfig);
            return batchSizeFailure.orElse(
                    Utils.fireValidators(Either.right(validatable), validationConfig.getValidatorsStream(), throwableMapper)
                            .filter(Either::isLeft)
                            .findFirst()
                            .map(Either::swap)
                            .map(Either::toOption)
                            .orElse(Option.none()));
        };
    }

    @FunctionalInterface
    interface SimpleFailFastStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, FailureT> {
    }

    @FunctionalInterface
    interface SimpleFailFastStrategyForHeader<ValidatableT, FailureT> extends Function1<ValidatableT, Option<FailureT>> {
    }

}
