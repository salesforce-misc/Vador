package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.revcloud.vader.types.validators.Validator;

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
            val toBeValidatedRight = Either.<FailureT, ValidatableT>right(toBeValidated);
            return Utils.fireValidators(toBeValidatedRight, validators.iterator(), throwableMapper)
                    .find(Either::isLeft)
                    .getOrElse(toBeValidatedRight);
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
            return Utils.findFirstFailure(toBeValidatedRight, validationConfig, throwableMapper);
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
            val filteredValidatables =
                    Utils.filterInvalidatablesAndDuplicates(validatables, invalidValidatable, validationConfig);
            return filteredValidatables
                    .map(validatable -> Utils.findFirstFailure(validatable, validationConfig, throwableMapper)).toList();
        };
    }

    static <FailureT, ValidatableT> FailFastAllOrNoneForBatch<ValidatableT, FailureT> failFastAllOrNoneForBatch(
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            BatchValidationConfig<ValidatableT, FailureT> batchValidationConfig) {
        return validatables -> Utils.filterInvalidatablesAndDuplicatesForAllOrNone(validatables, invalidValidatable, batchValidationConfig)
                .orElse(validatables.iterator().map(Either::<FailureT, ValidatableT>right)
                        .map(validatable -> Utils.findFirstFailure(validatable, batchValidationConfig, throwableMapper))
                        .find(Either::isLeft).map(Either::getLeft));
    }

    @FunctionalInterface
    interface FailFast<ValidatableT, FailureT> extends Function1<ValidatableT, Either<FailureT, ValidatableT>> {
    }

    @FunctionalInterface
    interface FailFastForBatch<ValidatableT, FailureT> extends Function1<List<ValidatableT>, List<Either<FailureT, ValidatableT>>> {
    }

    @FunctionalInterface
    interface FailFastAllOrNoneForBatch<ValidatableT, FailureT> extends Function1<List<ValidatableT>, Option<FailureT>> {
    }
}

