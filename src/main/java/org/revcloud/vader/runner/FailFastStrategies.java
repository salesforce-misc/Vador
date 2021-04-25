package org.revcloud.vader.runner;

import io.vavr.Function1;

import java.util.List;
import java.util.Optional;

import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.revcloud.vader.types.validators.Validator;

import static org.revcloud.vader.runner.Utils.filterInvalidatablesAndDuplicates;
import static org.revcloud.vader.runner.Utils.findFirstFailure;

@UtilityClass
class FailFastStrategies {

    /**
     * Higher-order function to compose list of validators into Fail-Fast Strategy.
     *
     * @param validators
     * @param invalidValidatable
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Composed Fail-Fast Strategy
     */
    static <FailureT, ValidatableT> FailFast<ValidatableT, FailureT> failFast(
            @NonNull List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper) {
        return toBeValidated -> {
            if (toBeValidated == null) return Either.left(invalidValidatable);
            val validatable = Either.<FailureT, ValidatableT>right(toBeValidated);
            return Utils.fireValidators(validatable, validators.stream(), throwableMapper)
                    .filter(Either::isLeft)
                    .findFirst()
                    .orElse(validatable);
        };
    }

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
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return toBeValidated -> {
            if (toBeValidated == null) return Either.left(invalidValidatable);
            val toBeValidatedRight = Either.<FailureT, ValidatableT>right(toBeValidated);
            return findFirstFailure(toBeValidatedRight, validationConfig, throwableMapper);
        };
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

    @FunctionalInterface
    interface FailFast<ValidatableT, FailureT> extends Function1<ValidatableT, Either<FailureT, ValidatableT>> {
    }

    @FunctionalInterface
    interface FailFastForBatch<ValidatableT, FailureT> extends Function1<List<ValidatableT>, List<Either<FailureT, ValidatableT>>> {
    }

    @FunctionalInterface
    interface FailFastAllOrNoneForBatch<ValidatableT, FailureT> extends Function1<List<ValidatableT>, Optional<FailureT>> {
    }
}

