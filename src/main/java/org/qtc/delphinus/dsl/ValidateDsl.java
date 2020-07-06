package org.qtc.delphinus.dsl;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import org.qtc.delphinus.Strategies;
import org.qtc.delphinus.types.validators.Validator;
import org.qtc.delphinus.types.validators.simple.SimpleValidator;

/**
 * DSL for different ways to run validations against a validatable.
 *
 * @author gakshintala
 * @since 228
 */
@UtilityClass
public class ValidateDsl {

    /**
     * Applies the validators on validatable in fail-fast mode.
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
            FailureT invalidValidatable, FailureT none) {
        final Either<FailureT, ?> validationResult
                = Strategies.failFastStrategy(validators, invalidValidatable).apply(validatable);
        return validationResult.fold(Function1.identity(), ignore -> none);
    }

    /**
     * Applies the Simple validators on validatable in fail-fast mode.
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
     * Validates a list of validatables agains a list of validations, in fail-fast mode, per validatable.
     *
     * @param validatables       List of validatables.
     * @param validators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return List of Validation failures.
     */
    public static <FailureT, ValidatableT> List<Either<FailureT, ?>> validateAndFailFast(
            List<ValidatableT> validatables, List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable) {
        return validatables.iterator()
                .map(Strategies.failFastStrategy(validators, invalidValidatable))
                .toList();
    }

    /**
     * Applies the Simple validators on validatable in error-accumulation mode.
     *
     * @param validatable
     * @param validators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param none               Value to be returned in case of no failure.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return List of Validation failures.
     */
    public static <FailureT, ValidatableT> List<FailureT> validateAndAccumulateErrors(
            ValidatableT validatable, List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable, FailureT none) {
        final List<Either<FailureT, ?>> validationResults
                = Strategies.accumulationStrategy(validators, invalidValidatable).apply(validatable);
        return validationResults.map(validationResult -> validationResult.fold(Function1.identity(), ignore -> none));
    }

    /**
     * Validates a list of validatables agains a list of validations, in error-accumulation mode, per validatable.
     *
     * @param validatables
     * @param validators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return List of Validation failures.
     */
    public static <FailureT, ValidatableT> List<List<Either<FailureT, ?>>> validateAndAccumulateErrors(
            List<ValidatableT> validatables, List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable) {
        return validatables.iterator()
                .map(Strategies.accumulationStrategy(validators, invalidValidatable))
                .toList();
    }
}
