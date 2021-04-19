package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.revcloud.vader.types.validators.Validator;

@Slf4j
@UtilityClass
class FailFast {

    /**
     * Higher-order function to compose list of validators into Fail-Fast Strategy.
     *
     * @param validators
     * @param invalidValidatable
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Composed Fail-Fast Strategy
     */
    static <FailureT, ValidatableT> FailFastStrategy<ValidatableT, FailureT> failFastStrategy(
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
     * @param validators
     * @param invalidValidatable
     * @param throwableMapper
     * @param validationConfig
     * @param <FailureT>
     * @param <ValidatableT>
     * @return
     */
    static <FailureT, ValidatableT> FailFastStrategy<ValidatableT, FailureT> failFastStrategy(
            @NonNull List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return toBeValidated -> {
            if (toBeValidated == null) return Either.left(invalidValidatable);
            val toBeValidatedRight = Either.<FailureT, ValidatableT>right(toBeValidated);
            return Utils.fireValidators(toBeValidatedRight, Iterator.concat(Utils.toValidators(validationConfig), validators), throwableMapper)
                    .find(Either::isLeft)
                    .getOrElse(toBeValidatedRight);
        };
    }

    /**
     * Batch + Simple + Config  
     * @param invalidValidatable
     * @param throwableMapper
     * @param validationConfig
     * @param <FailureT>
     * @param <ValidatableT>
     * @return
     */
    static <FailureT, ValidatableT> FailFastStrategyForBatch<ValidatableT, FailureT> failFastStrategyForBatch(
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            BatchValidationConfig<ValidatableT, FailureT> validationConfig) {
        return validatables -> {
            final Seq<Either<FailureT, ValidatableT>> validatablesEither =
                    Utils.filterInvalidatablesAndDuplicates(validatables, invalidValidatable, validationConfig);
            return validatablesEither.map(validatableEither -> Utils.toValidators(validationConfig).iterator()
                    .map(currentValidation -> Utils.fireValidator(currentValidation, validatableEither, throwableMapper))
                    .find(Either::isLeft)
                    .getOrElse(validatableEither)).toList();
        };
    }

    @FunctionalInterface
    interface FailFastStrategy<ValidatableT, FailureT> extends Function1<ValidatableT, Either<FailureT, ValidatableT>> {
    }

    @FunctionalInterface
    interface FailFastStrategyForBatch<ValidatableT, FailureT> extends Function1<List<ValidatableT>, List<Either<FailureT, ValidatableT>>> {
    }
}

