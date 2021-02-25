package org.qtc.delphinus.dsl.runner;

import io.vavr.Function1;
import io.vavr.collection.List;
import lombok.experimental.UtilityClass;
import org.qtc.delphinus.dsl.lift.LiftDsl;
import org.qtc.delphinus.types.validators.Validator;
import org.qtc.delphinus.types.validators.simple.SimpleValidator;

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
            FailureT none) {
        return Strategies.failFastStrategy(validators, invalidValidatable).apply(validatable)
                .fold(Function1.identity(), ignore -> none);
    }

    public static <FailureT, ValidatableT> FailureT validateAndFailFast(
            ValidatableT validatable, List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable,
            FailureT none,
            Function1<Throwable, FailureT> throwableMapper) {
        return Strategies.failFastStrategy(validators, invalidValidatable, throwableMapper).apply(validatable)
                .fold(Function1.identity(), ignore -> none);
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
            ValidatableT validatable, List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable, FailureT none) {
        return Strategies.failFastStrategy(validators, invalidValidatable, none).apply(validatable);
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
     * @return List of Validation failures. EmptyList if all the validations pass.
     */
    public static <FailureT, ValidatableT> List<FailureT> validateAndAccumulateErrors(
            ValidatableT validatable, List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable, FailureT none) {
        final var results = Strategies.accumulationStrategy(validators, invalidValidatable).apply(validatable)
                .map(validationResult -> validationResult.fold(Function1.identity(), ignore -> none));
        return results.forAll(result -> result == none) ? List.empty(): results;
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
            FailureT invalidValidatable, FailureT none) {
        return validateAndAccumulateErrors(validatable, LiftDsl.liftAllSimple(simpleValidators, none), invalidValidatable, none);
    }

}
