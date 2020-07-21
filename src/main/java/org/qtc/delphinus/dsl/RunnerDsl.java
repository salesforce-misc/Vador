package org.qtc.delphinus.dsl;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import org.qtc.delphinus.types.validators.Validator;
import org.qtc.delphinus.types.validators.simple.SimpleValidator;

/**
 * DSL for different ways to run validations against a validatable.
 *
 * @author gakshintala
 * @since 228
 */
@UtilityClass
public class RunnerDsl {

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
     * This can be used to validate a batch of beans together. It Validates a list of
     * validatables against a list of SimpleValidations, in fail-fast mode, per validatable.
     *
     * @param validatables
     * @param validators
     * @param invalidValidatable       FailureT if the validatable is null.
     * @param none
     * @param fillRightWithValidatable If true, all the valid Eithers (Eithers in right state) are filled with validatables
     *                                 on the right state. This helps in making further processing on these bag of results
     *                                 skipping the invalid ones.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return This list is a bag of results for both Valids and Invalids.
     * Valids are represented by right state of Either and Invalids with left (holding Validation Failures).
     */
    public static <FailureT, ValidatableT> List<Either<FailureT, ?>> validateAndFailFastForSimpleValidators(
            List<ValidatableT> validatables, List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable, FailureT none, boolean fillRightWithValidatable) {
        return validateAndFailFast(validatables, Dsl.liftAllSimple(validators, none), invalidValidatable, fillRightWithValidatable);
    }

    /**
     * This can be used to validate a batch of beans together. It Validates a list of
     * validatables against a list of Validations, in fail-fast mode, per validatable.
     *
     * @param validatables             List of validatables.
     * @param validators
     * @param invalidValidatable       FailureT if the validatable is null.
     * @param fillRightWithValidatable If true, all the valid Eithers (Eithers in right state) are filled with validatables
     *                                 on the right state. This helps in making further processing on these bag of results
     *                                 skipping the invalid ones.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return This list is a bag of results for both Valids and Invalids.
     * Valids are represented by right state of Either and Invalids with left (holding Validation Failures).
     */
    public static <FailureT, ValidatableT> List<Either<FailureT, ?>> validateAndFailFast(
            List<ValidatableT> validatables, List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable, boolean fillRightWithValidatable) {
        final List<Either<FailureT, ?>> validationResults = validatables.iterator()
                .map(Strategies.failFastStrategy(validators, invalidValidatable))
                .toList();
        if (fillRightWithValidatable) {
            return validationResults.zip(validatables)
                    .map(resultToValidatable -> resultToValidatable._1.isRight()
                            ? Either.right(resultToValidatable._2)
                            : resultToValidatable._1);
        } else {
            return validationResults;
        }
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
     * Validates a list of validatables against a list of validations, in error-accumulation mode, per validatable.
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
