package org.qtc.delphinus.dsl.runner;

import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.experimental.UtilityClass;
import org.qtc.delphinus.dsl.lift.LiftDsl;
import org.qtc.delphinus.types.validators.Validator;
import org.qtc.delphinus.types.validators.simple.SimpleValidator;

/**
 * DSL for different ways to run validations against a List of validatables (Batch).
 *
 * @author gakshintala
 * @since 228
 */
@UtilityClass
public class BatchRunnerDsl {
    /**
     * This can be used to validate a batch of beans together. It Validates a list of
     * validatables against a list of Validations, in fail-fast mode, per validatable.
     *
     * @param validatables             List of validatables.
     * @param validators
     * @param invalidValidatable       FailureT if the validatable is null.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return This list is a bag of results for both Valids and Invalids.
     * Valids are represented by right state of Either and Invalids with left (holding Validation Failures).
     */
    public static <FailureT, ValidatableT> List<Either<FailureT, ValidatableT>> validateAndFailFast(
            List<ValidatableT> validatables, List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable) {
        return validatables.iterator()
                .map(Strategies.failFastStrategy(validators, invalidValidatable))
                .toList();
    }

    /**
     * Validates a list of validatables against a list of Simple Validations, in fail-fast mode, per validatable.
     *
     * @param validatables
     * @param validators
     * @param invalidValidatable       FailureT if the validatable is null.
     * @param none
     * @param <FailureT>
     * @param <ValidatableT>
     * @return This list is a bag of results for both Valids and Invalids.
     * Valids are represented by right state of Either and Invalids with left (holding Validation Failures).
     */
    public static <FailureT, ValidatableT> List<Either<FailureT, ValidatableT>> validateAndFailFast(
            List<ValidatableT> validatables, List<SimpleValidator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable, FailureT none) {
        return validateAndFailFast(validatables, LiftDsl.liftAllSimple(validators, none), invalidValidatable);
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
    public static <FailureT, ValidatableT> List<List<Either<FailureT, ValidatableT>>> validateAndAccumulateErrors(
            List<ValidatableT> validatables, List<Validator<ValidatableT, FailureT>> validators,
            FailureT invalidValidatable) {
        return validatables.iterator()
                .map(Strategies.accumulationStrategy(validators, invalidValidatable))
                .toList();
    }

    /**
     * Validates a list of validatables against a list of Simple validations, in error-accumulation mode, per validatable.
     *
     * @param validatables
     * @param simpleValidators
     * @param invalidValidatable FailureT if the validatable is null.
     * @param <FailureT>
     * @param <ValidatableT>
     * @return List of Validation failures.
     */
    public static <FailureT, ValidatableT> List<List<Either<FailureT, ValidatableT>>> validateAndAccumulateErrors(
            List<ValidatableT> validatables, List<SimpleValidator<ValidatableT, FailureT>> simpleValidators,
            FailureT invalidValidatable, FailureT none) {
        return validateAndAccumulateErrors(validatables, LiftDsl.liftAllSimple(simpleValidators, none), invalidValidatable);
    }
}
