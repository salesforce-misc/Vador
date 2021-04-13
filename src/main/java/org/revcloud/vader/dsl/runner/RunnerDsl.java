package org.revcloud.vader.dsl.runner;

import io.vavr.Function1;
import io.vavr.collection.List;
import lombok.experimental.UtilityClass;
import org.revcloud.vader.dsl.lift.ValidatorLiftDsl;
import org.revcloud.vader.types.validators.Validator;
import org.revcloud.vader.types.validators.SimpleValidator;

/**
 * DSL for different ways to run validations against a Single validatable (Non-Batch).
 *
 * @author gakshintala
 * @since 228
 */
@UtilityClass
public class RunnerDsl {

    /**
     * Applies the validators on a Single validatable in fail-fast mode.
     *
     * @param validatable
     * @param validators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param none               Value to be returned in case of no failure.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Validation failure
     */
    public static <FailureT, ValidatableT> FailureT validateAndFailFast(
            ValidatableT validatable, List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper) {
        return FailFast.failFastStrategy(validators, invalidValidatable, throwableMapper).apply(validatable)
                .fold(Function1.identity(), ignore -> none);
    }

    public static <FailureT, ValidatableT> FailureT validateAndFailFast(
            ValidatableT validatable, List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return FailFast.failFastStrategy(validators, invalidValidatable, throwableMapper, validationConfig).apply(validatable)
                .fold(Function1.identity(), ignore -> none);
    }

    /**
     * Applies the validators on a Single validatable in error-accumulation mode. The Accumulated
     *
     * @param validatable
     * @param validators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param none               Value to be returned in case of no failure.
     * @param <FailureT>
     * @param <ValidatableT>
     * @param throwableMapper   Function to map throwable to Failure in case of exception
     * @return List of Validation failures. EmptyList if all the validations pass.
     */
    public static <FailureT, ValidatableT> List<FailureT> validateAndAccumulateErrors(
            ValidatableT validatable, List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable, FailureT none, Function1<Throwable, FailureT> throwableMapper) {
        final var results = Accumulation.accumulationStrategy(validators, invalidValidatable, throwableMapper).apply(validatable)
                .map(validationResult -> validationResult.fold(Function1.identity(), ignore -> none));
        return results.forAll(result -> ((result == none) || result.equals(none))) ? List.empty(): results;
    }

    /**
     * Applies the Simple validators on a Single validatable in fail-fast mode.
     *
     * @param validatable
     * @param validators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param none               Value to be returned in case of no failure.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return Validation failure.
     */
    public static <FailureT, ValidatableT> FailureT validateAndFailFastForSimpleValidators(
            ValidatableT validatable,
            List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper) {
        return FailFastSimple.failFastStrategy(validators, invalidValidatable, none, throwableMapper).apply(validatable);
    }

    public static <FailureT, ValidatableT> FailureT validateAndFailFastForSimpleValidators(
            ValidatableT validatable,
            List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return FailFastSimple.failFastStrategy(validators, invalidValidatable, none, throwableMapper, validationConfig).apply(validatable);
    }

    public static <FailureT, ValidatableT> FailureT validateAndFailFastForSimpleValidatorsForHeader(
            ValidatableT validatable,
            List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper,
            HeaderValidationConfig<ValidatableT, FailureT> validationConfig) {
        return FailFastSimple.failFastStrategyForHeader(validators, invalidValidatable, none, throwableMapper, validationConfig).apply(validatable);
    }

    /**
     * Applies the Simple validators on a Single validatable in error-accumulation mode.
     *
     * @param validatable
     * @param simpleValidators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param none               Value to be returned in case of no failure.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return List of Validation failures.
     */
    public static <FailureT, ValidatableT> List<FailureT> validateAndAccumulateErrorsForSimpleValidators(
            ValidatableT validatable, List<SimpleValidator<ValidatableT, FailureT>> simpleValidators,
            FailureT invalidValidatable, FailureT none, Function1<Throwable, FailureT> throwableMapper) {
        return validateAndAccumulateErrors(validatable, ValidatorLiftDsl.liftAllSimple(simpleValidators, none), invalidValidatable, none, throwableMapper);
    }

}
