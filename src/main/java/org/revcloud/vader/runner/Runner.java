package org.revcloud.vader.runner;

import io.vavr.Function1;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.revcloud.vader.lift.ValidatorLiftUtil;
import org.revcloud.vader.types.validators.SimpleValidator;
import org.revcloud.vader.types.validators.Validator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DSL for different ways to run validations against a Single validatable (Non-Batch).
 *
 * @author gakshintala
 * @since 228
 */
@UtilityClass
public class Runner {

    public static <FailureT, ValidatableT> Optional<FailureT> validateAndFailFastForHeader(
            ValidatableT validatable,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            HeaderValidationConfig<ValidatableT, FailureT> validationConfig) {
        return FailFastStrategies.failFastStrategyForHeader(invalidValidatable, throwableMapper, validationConfig).apply(validatable);
    }

    public static <FailureT, ValidatableT> Optional<FailureT> validateAndFailFast(
            ValidatableT validatable,
            FailureT invalidValidatable,
            Function1<Throwable, FailureT> throwableMapper,
            ValidationConfig<ValidatableT, FailureT> validationConfig) {
        return FailFastStrategies.failFast(invalidValidatable, throwableMapper, validationConfig).apply(validatable)
                .swap().toJavaOptional();
    }
    
    // --- ERROR ACCUMULTATION ---
    
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
        return validateAndAccumulateErrors(validatable, ValidatorLiftUtil.liftAllSimple(simpleValidators, none), invalidValidatable, none, throwableMapper);
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
        val results = AccumulationStrategies.accumulationStrategy(validators, invalidValidatable, throwableMapper).apply(validatable).stream()
                .map(validationResult -> validationResult.fold(Function1.identity(), ignore -> none));
        return results.allMatch(result -> ((result == none) || result.equals(none))) ? Collections.emptyList() : results.collect(Collectors.toList());
    }

}
